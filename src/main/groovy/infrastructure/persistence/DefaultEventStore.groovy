package infrastructure.persistence

import domain.events.EventEnvelope

abstract class DefaultEventStore implements EventStore {

    abstract save(EventEnvelope eventEnvelope)

    def getAggregate(String applicationName, String boundedContextName, String aggregateName, Class aggregateClass, UUID aggregateId, String eventPackageName) throws UnknownAggregate {
        def aggregateEvents = getEventsFor(applicationName, boundedContextName, aggregateName, aggregateId, eventPackageName)
        if(aggregateEvents.empty) { throw new UnknownAggregate(aggregateClass, aggregateId) }

        def aggregate = aggregateClass.newInstance()

        aggregate.apply(aggregateEvents)

        return aggregate
//        def deviceHistory = getEventsFor(Device, command.deviceId)
//        Device device = deviceHistory.inject null, { device, event ->
//            event.applyTo device
//        }
    }

    abstract getEventsFor(String applicationName, String boundedContextName, String aggregateName, UUID aggregateId, String eventPackageName)

}
