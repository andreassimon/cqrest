package infrastructure.persistence

import domain.events.EventEnvelope

interface EventStore {

    def save(EventEnvelope eventEnvelope)
    def getAggregate(Class aggregateClass, UUID aggregateId)
    def getEventsFor(String applicationName, String boundedContextName, String aggregateName, UUID aggregateId)

}
