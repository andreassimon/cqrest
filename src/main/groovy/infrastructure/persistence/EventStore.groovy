package infrastructure.persistence

import domain.events.EventEnvelope

public interface EventStore {

    def save(EventEnvelope eventEnvelope)
    def getAggregate(String applicationName, String boundedContextName, String aggregateName, Class aggregateClass, UUID aggregateId)
    def getEventsFor(String applicationName, String boundedContextName, String aggregateName, UUID aggregateId)

}
