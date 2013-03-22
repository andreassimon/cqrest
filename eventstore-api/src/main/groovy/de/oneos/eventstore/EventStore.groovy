package de.oneos.eventstore

import de.oneos.eventsourcing.*


interface EventStore {

    void setPublishers(List<EventPublisher> eventPublishers)

    void inUnitOfWork(Closure closure)

    UnitOfWork createUnitOfWork()

    void commit(UnitOfWork unitOfWork) throws IllegalArgumentException, EventCollisionOccurred

    List<EventEnvelope> loadEventEnvelopes(UUID aggregateId)

}
