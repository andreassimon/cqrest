package de.oneos.eventstore

import de.oneos.eventsourcing.*


interface EventStore {

    UnitOfWork createUnitOfWork()

    // TODO replace method with transactional commit(UnitOfWork)
    void save(EventEnvelope eventEnvelope) throws IllegalArgumentException, EventCollisionOccurred

    List<EventEnvelope> loadEventEnvelopes(String applicationName, String boundedContextName, String aggregateName, UUID aggregateId, Closure<Event> eventFactory)

}
