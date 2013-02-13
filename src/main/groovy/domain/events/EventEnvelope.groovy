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

    EventEnvelope(String applicationName, String boundedContextName, String aggregateName, UUID aggregateId, Event<AggregateType> event, Date timestamp = new Date()) {
        this.applicationName = applicationName
        this.boundedContextName = boundedContextName
        this.aggregateName = aggregateName
        this.aggregateId = aggregateId
        this.event = event
        this.timestamp = timestamp
    }

    String getEventName() {
        return event.name
    }

    String getSerializedEvent() {
        return GenericEventSerializer.toJSON(event)
    }

    @Override
    String toString() {
        "EventEnvelope[$applicationName.$boundedContextName.$aggregateName{$aggregateId} @${timestamp.format('yyyy-MM-dd HH:mm:ss.SSS')} :: <$event>]".toString()
    }

    @Override
    boolean equals(that) {
        this.applicationName == that.applicationName &&
                this.boundedContextName == that.boundedContextName &&
                this.aggregateName == that.aggregateName &&
                this.aggregateId   == that.aggregateId &&
                this.event         == that.event &&
                this.timestamp     == that.timestamp
    }
}
