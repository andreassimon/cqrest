package de.oneos.eventstore

import de.oneos.eventsourcing.*


interface EventStore {

    UnitOfWork createUnitOfWork()

    // TODO replace method with transactional commit(UnitOfWork)
    void save(EventEnvelope eventEnvelope) throws IllegalArgumentException, EventCollisionOccurred

    List loadEvents(String applicationName, String boundedContextName, String aggregateName, UUID aggregateId, Closure<Object> eventFactory)

    def buildAggregate(Class aggregateClass, String applicationName, String boundedContextName, String aggregateName, UUID aggregateId, Closure eventFactory)

}
