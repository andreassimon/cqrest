package de.oneos.cqrs.eventstore

abstract class DefaultEventStore implements EventStore {

    def getAggregate(String applicationName, String boundedContextName, String aggregateName, Class aggregateClass, UUID aggregateId, String eventPackageName) throws UnknownAggregate {
        def aggregateEvents = getEventsFor(applicationName, boundedContextName, aggregateName, aggregateId, eventPackageName)
        if(aggregateEvents.empty) { throw new UnknownAggregate(aggregateClass, aggregateId) }

        return aggregateEvents.inject(aggregateClass.newInstance()) { aggregate, event ->
            event.applyTo(aggregate)
        }
    }

}
