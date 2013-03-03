package de.oneos.eventstore

import de.oneos.eventsourcing.*


interface EventStore {

    void setPublishers(List<EventPublisher> eventPublishers)

    UnitOfWork createUnitOfWork()

    void commit(UnitOfWork unitOfWork) throws IllegalArgumentException, EventCollisionOccurred

    List<EventEnvelope> loadEventEnvelopes(String applicationName, String boundedContextName, String aggregateName, UUID aggregateId, Closure<Event> eventFactory)

    void inUnitOfWork(Closure closure)

}
