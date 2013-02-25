package de.oneos.eventstore

import de.oneos.eventsourcing.*


interface EventStore {

    UnitOfWork createUnitOfWork()

    void save(EventEnvelope eventEnvelope) throws IllegalArgumentException, EventCollisionOccurred

    List loadEvents(String applicationName, String boundedContextName, String aggregateName, UUID aggregateId, Closure<Object> eventFactory)

}
