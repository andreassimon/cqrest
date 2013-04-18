package de.oneos.eventstore.inmemory

import de.oneos.eventstore.*
import de.oneos.eventsourcing.Event

import static de.oneos.eventstore.EventStore.*


class InMemoryEventStore implements EventStore {
    List<EventEnvelope> history = []
    Collection<EventPublisher> eventPublishers = []

    @Override
    void setPublishers(List<EventPublisher> eventPublishers) {
        assert null != eventPublishers
        this.eventPublishers = eventPublishers
    }

    @Override
    void addPublisher(EventPublisher eventPublisher) {
        assert null != eventPublisher
        eventPublishers.add(eventPublisher)
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
        unitOfWork.eachEventEnvelope { EventEnvelope it ->
            AssertEventEnvelope.isValid(it)
            assertIsUnique(it)
        }
        unitOfWork.eachEventEnvelope {
            history << it
            eventPublishers.each { publisher ->
                try {
                    publisher.publish(it)
                } catch (all) { }
            }
        }
        unitOfWork.flush()
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
        AssertEventEnvelope.isValid(newEnvelope)
        assertIsUnique(newEnvelope)
        history << newEnvelope
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
    List<EventEnvelope> loadEventEnvelopes(UUID aggregateId) {
        return findAll(aggregateId: aggregateId)
    }

    @Override
    List<EventEnvelope> findAll(Map<String, ?> criteria) {
        return history.findAll {
            criteria.every { attribute, value ->
                value == it[attribute]
            }
        }
    }

    @Override
    String toString() { "InMemoryEventStore" }

}

