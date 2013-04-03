package de.oneos.eventstore.inmemory

import de.oneos.eventstore.*
import de.oneos.eventsourcing.Event

import static de.oneos.eventstore.EventStore.*


class InMemoryEventStore implements EventStore {
    List<EventEnvelope> history = []
    Collection<EventPublisher> eventPublishers = []
    String application
    String boundedContext

    InMemoryEventStore(String application, String boundedContext) {
        assert application != null
        assert boundedContext != null

        this.application = application
        this.boundedContext = boundedContext
    }

    @Override
    void setPublishers(List<EventPublisher> eventPublishers) {
        this.eventPublishers = eventPublishers
    }

    @Override
    void inUnitOfWork(UUID correlationId, String user, Closure closure) {
        def unitOfWork = createUnitOfWork(correlationId, user)
        unitOfWork.with closure
        commit(unitOfWork)
    }

    @Override
    UnitOfWork createUnitOfWork(UUID correlationId, String user) {
        new UnitOfWork(this, application, boundedContext, correlationId, user)
    }

    @Override
    void commit(UnitOfWork unitOfWork) {
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

    void addEventEnvelope(UUID aggregateId, Event event, int sequenceNumber, String user) {
        EventEnvelope newEnvelope = new EventEnvelope(
            this.application,
            this.boundedContext,
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

}

