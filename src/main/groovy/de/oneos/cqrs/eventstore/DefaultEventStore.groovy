package de.oneos.cqrs.eventstore

import domain.events.EventEnvelope
import de.oneos.cqrs.eventstore.EventStore
import infrastructure.persistence.UnknownAggregate

abstract class DefaultEventStore implements EventStore {

    abstract save(EventEnvelope eventEnvelope)

    def getAggregate(String applicationName, String boundedContextName, String aggregateName, Class aggregateClass, UUID aggregateId, String eventPackageName) throws UnknownAggregate {
        def aggregateEvents = getEventsFor(applicationName, boundedContextName, aggregateName, aggregateId, eventPackageName)
        if(aggregateEvents.empty) { throw new UnknownAggregate(aggregateClass, aggregateId) }

        def aggregate = aggregateClass.newInstance()

        aggregate.apply(aggregateEvents)

        return aggregate
    }

    abstract getEventsFor(String applicationName, String boundedContextName, String aggregateName, UUID aggregateId, String eventPackageName)

}
