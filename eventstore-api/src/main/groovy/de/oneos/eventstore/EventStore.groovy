package de.oneos.eventstore

import de.oneos.eventsourcing.*


interface EventStore {

    void setPublishers(List<EventPublisher> eventPublishers)

    void inUnitOfWork(UUID correlationId, String user, Closure closure)

    UnitOfWork createUnitOfWork(UUID correlationId, String user)

    void commit(UnitOfWork unitOfWork) throws IllegalArgumentException, EventCollisionOccurred

    List<EventEnvelope> loadEventEnvelopes(UUID aggregateId)

}
