package de.oneos.eventstore.inmemory

import de.oneos.eventstore.*
import de.oneos.eventsourcing.*


class InMemoryEventStore implements EventStore {
    List<EventEnvelope> history = []
    List<EventPublisher> eventPublishers

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
        new UnitOfWork(this)
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
    List<EventEnvelope> loadEventEnvelopes(String applicationName, String boundedContextName, String aggregateName, UUID aggregateId, Closure<Event> eventFactory) {
        return history.findAll {
            applicationName == it.applicationName &&
            boundedContextName == it.boundedContextName &&
            aggregateName == it.aggregateName &&
            aggregateId == it.aggregateId
        }
    }
}

