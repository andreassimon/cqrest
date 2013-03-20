package de.oneos.eventstore

import de.oneos.eventsourcing.*


interface EventStore {

    void setPublishers(List<EventPublisher> eventPublishers)

    void inBoundedContext(String application, String boundedContext, Closure closure)

    UnitOfWork createUnitOfWork(String application, String boundedContext)

    void commit(UnitOfWork unitOfWork) throws IllegalArgumentException, EventCollisionOccurred

    List<EventEnvelope> loadEventEnvelopes(String applicationName, String boundedContextName, String aggregateName, UUID aggregateId, Closure<Event> eventFactory)

}
