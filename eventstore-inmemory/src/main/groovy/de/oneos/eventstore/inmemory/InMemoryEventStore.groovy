package de.oneos.eventstore.inmemory

import de.oneos.eventsourcing.Correlation
import de.oneos.eventsourcing.EventBus
import org.apache.commons.logging.*

import de.oneos.eventstore.*
import de.oneos.eventsourcing.Event


class InMemoryEventStore implements EventStore {
    static Log log = LogFactory.getLog(InMemoryEventStore)


    List<EventEnvelope> history = []
    Collection<EventConsumer> eventConsumers = []

    @Override
    void setEventConsumers(List<EventConsumer> eventConsumers) {
        assert null != eventConsumers
        this.eventConsumers.clear()
        eventConsumers.each { subscribeTo(it) }
    }

    @Override
    EventSupplier rightShift(EventConsumer eventConsumer) {
        subscribeTo(eventConsumer)
        return this
    }

    @Override
    void subscribeTo(EventConsumer eventConsumer) {
        subscribeTo(eventConsumer.eventCriteria, eventConsumer)
    }

    @Override
    void subscribeTo(Map<String, ?> criteria, EventConsumer eventConsumer) {
        assert null != eventConsumer
        eventConsumers.add(eventConsumer)
        try {
            eventConsumer.wasRegisteredAt(this)
        } catch(e) {
            log.warn("Exception occurred during registration of $eventConsumer at $this", e)
        }
    }

    @Override
    public Correlation inUnitOfWork(String application, String boundedContext, UUID correlationId, String user, Closure closure) {
        Correlation correlation = new Correlation(correlationId)
        EventBus.subscribeCorrelation(correlation)
        def unitOfWork = createUnitOfWork(application, boundedContext, correlationId, user)
        unitOfWork.with(closure)
        commit(unitOfWork)
        return correlation
    }

    @Override
    UnitOfWork createUnitOfWork(String application, String boundedContext, UUID correlationId, String user) {
        new UnitOfWork(this, application, boundedContext, correlationId, user)
    }

    @Override
    void commit(UnitOfWork unitOfWork) throws IllegalArgumentException, EventCollisionOccurred {
        unitOfWork.eachEventEnvelope validateEnvelope
        unitOfWork.eachEventEnvelope saveEnvelope
        unitOfWork.flush()
    }

    protected Closure validateEnvelope = { EventEnvelope it ->
        AssertEventEnvelope.isValid(it)
        assertIsUnique(it)
    }

    protected Closure saveEnvelope = { eventEnvelope ->
        history << eventEnvelope
        eventConsumers.each { processor ->
            try {
                processor.process(eventEnvelope)
            } catch(e) {
                log.error("Couldn't process $eventEnvelope with $processor", e)
            }
        }
    }

    void addEventEnvelope(UUID aggregateId, String application, String boundedContext, Event event, int sequenceNumber, String user) {
        EventEnvelope newEnvelope = new EventEnvelope(
            application,
            boundedContext,
            'UNKNOWN',
            aggregateId, event, sequenceNumber,
            NO_CORRELATION_ID,
            USER_UNKNOWN
        )
        validateEnvelope.call(newEnvelope)
        saveEnvelope.call(newEnvelope)
    }

    protected assertIsUnique(EventEnvelope eventEnvelope) {
        if (history.find { persistedEnvelope ->
            persistedEnvelope.aggregateId == eventEnvelope.aggregateId &&
                persistedEnvelope.sequenceNumber == eventEnvelope.sequenceNumber
        }) {
            throw new EventCollisionOccurred(eventEnvelope)
        }
    }

    @Override
    List<EventEnvelope> findAll(Map<String, ?> criteria) {
        return history.findAll { eventEnvelope ->
            criteria.every { attribute, value ->
                if(value instanceof Collection) {
                    return value.contains(eventEnvelope[attribute])
                }
                value == eventEnvelope[attribute]
            }
        }
    }

    @Override
    String toString() { "InMemoryEventStore" }

    @Override
    void withEventEnvelopes(Map<String, ?> criteria, Closure block) {
        findAll(criteria).each(block)
    }

}

