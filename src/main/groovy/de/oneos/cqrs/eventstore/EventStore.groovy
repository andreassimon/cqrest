package de.oneos.cqrs.eventstore

import domain.events.EventEnvelope
import infrastructure.persistence.UnknownAggregate

public interface EventStore {

    def save(EventEnvelope eventEnvelope)
    public <A> A getAggregate(String applicationName, String boundedContextName, String aggregateName, Class<A> aggregateClass, UUID aggregateId, String eventPackageName) throws UnknownAggregate
    List getEventsFor(String applicationName, String boundedContextName, String aggregateName, UUID aggregateId, String eventPackageName) throws UnknownAggregate

}
