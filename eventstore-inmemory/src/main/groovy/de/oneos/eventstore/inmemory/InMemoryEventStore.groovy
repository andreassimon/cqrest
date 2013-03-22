package de.oneos.eventstore.inmemory

import de.oneos.eventstore.*


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
    void inUnitOfWork(Closure closure) {
        def unitOfWork = createUnitOfWork()
        unitOfWork.with closure
        commit(unitOfWork)
    }

    @Override
    UnitOfWork createUnitOfWork() {
        new UnitOfWork(this, application, boundedContext)
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
        return history.findAll {
            aggregateId == it.aggregateId
        }
    }
}

