package de.oneos.cqrs.eventstore.inmemory

import de.oneos.eventstore.*
import de.oneos.eventsourcing.*


class InMemoryEventStore implements EventStore {
    def history = []

    @Override
    void setPublishers(List<EventPublisher> eventPublishers) {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    void inUnitOfWork(Closure closure) {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    UnitOfWork createUnitOfWork() {
        return null  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    void commit(UnitOfWork unitOfWork) {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    List<EventEnvelope> loadEventEnvelopes(String applicationName, String boundedContextName, String aggregateName, UUID aggregateId, Closure<Event> eventFactory) {
        return null  //To change body of implemented methods use File | Settings | File Templates.
    }
}

