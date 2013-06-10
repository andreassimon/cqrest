package de.oneos.eventstore.inmemory

import org.apache.commons.logging.*

import de.oneos.eventstore.*
import de.oneos.eventsourcing.Event


class InMemoryEventStore implements EventStore {
    static Log log = LogFactory.getLog(InMemoryEventStore)


    List<EventEnvelope> history = []
    Collection<EventProcessor> eventProcessors = []

    @Override
    void setEventProcessors(List<EventProcessor> eventProcessors) {
        assert null != eventProcessors
        this.eventProcessors.clear()
        eventProcessors.each { subscribeTo([:], it) }
    }

    @Override
    void subscribeTo(Map<String, ?> criteria, EventProcessor eventProcessor) {
        assert null != eventProcessor
        eventProcessors.add(eventProcessor)
        try {
            eventProcessor.wasRegisteredAt(this)
        } catch(e) {
            log.warn("Exception occurred during registration of $eventProcessor at $this", e)
        }
    }

    @Override
    public <T> T inUnitOfWork(String application, String boundedContext, UUID correlationId, String user, Closure<T> closure) {
        def unitOfWork = createUnitOfWork(application, boundedContext, correlationId, user)
        T result = unitOfWork.with(closure)
        commit(unitOfWork)
        return result
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
        eventProcessors.each { processor ->
            try {
                processor.process(eventEnvelope)
            } catch(all) { }
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
        // TODO
        throw new RuntimeException("Not implemented")
    }

}

