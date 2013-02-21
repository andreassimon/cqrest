package de.oneos.cqrs.eventstore

import domain.events.EventEnvelope
import infrastructure.persistence.UnknownAggregate

public interface EventStore {

    def save(EventEnvelope eventEnvelope)
    def getAggregate(String applicationName, String boundedContextName, String aggregateName, Class aggregateClass, UUID aggregateId, String eventPackageName) throws UnknownAggregate
    List getEventsFor(String applicationName, String boundedContextName, String aggregateName, UUID aggregateId, String eventPackageName) throws UnknownAggregate

}
