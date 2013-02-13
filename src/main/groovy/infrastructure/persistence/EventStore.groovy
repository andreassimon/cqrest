package infrastructure.persistence

import domain.events.EventEnvelope

public interface EventStore {

    def save(EventEnvelope eventEnvelope)
    def getAggregate(String applicationName, String boundedContextName, String aggregateName, Class aggregateClass, UUID aggregateId, String eventPackageName)
    def getEventsFor(String applicationName, String boundedContextName, String aggregateName, UUID aggregateId, String eventPackageName)

}
