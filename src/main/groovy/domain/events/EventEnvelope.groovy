package domain.events

import infrastructure.utilities.GenericEventSerializer

class EventEnvelope<AggregateType> {

    final Date timestamp
    final Class<AggregateType> aggregateClass
    final String applicationName
    final String boundedContextName
    final String aggregateName
    final UUID aggregateId
    final Event<AggregateType> event

    EventEnvelope(Date timestamp = new Date(), String applicationName, String boundedContextName, String aggregateName, UUID aggregateId, Event<AggregateType> event) {
        this.timestamp = timestamp
        this.applicationName = applicationName
        this.boundedContextName = boundedContextName
        this.aggregateName = aggregateName
        this.aggregateId = aggregateId
        this.event = event
    }

    String getEventName() {
        return event.name
    }

    String getSerializedEvent() {
        return GenericEventSerializer.toJSON(event)
    }

}
