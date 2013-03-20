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
            if(history.find { persistedEnvelope ->
                persistedEnvelope.applicationName == it.applicationName &&
                persistedEnvelope.boundedContextName == it.boundedContextName &&
                persistedEnvelope.aggregateName == it.aggregateName &&
                persistedEnvelope.aggregateId == it.aggregateId &&
                persistedEnvelope.sequenceNumber == it.sequenceNumber
            }) {
                throw new EventCollisionOccurred(it)
            }
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

    @Override
    List<EventEnvelope> loadEventEnvelopes(String aggregateName, UUID aggregateId) {
        return history.findAll {
            application == it.applicationName &&
            boundedContext == it.boundedContextName &&
            aggregateName == it.aggregateName &&
            aggregateId == it.aggregateId
        }
    }
}

