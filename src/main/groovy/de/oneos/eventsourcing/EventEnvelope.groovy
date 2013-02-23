package de.oneos.eventsourcing

class EventEnvelope<AggregateType> {

    static final String TIMESTAMP_FORMAT = 'yyyy-MM-dd HH:mm:ss.SSS'
    final Date timestamp
    final Class<AggregateType> aggregateClass
    final String applicationName
    final String boundedContextName
    final String aggregateName
    final UUID aggregateId
    final Integer sequenceNumber
    final Event<AggregateType> event

    EventEnvelope(String applicationName, String boundedContextName, String aggregateName, UUID aggregateId, Event<AggregateType> event, int sequenceNumber = 0, Date timestamp = new Date()) {
        this.applicationName = applicationName
        this.boundedContextName = boundedContextName
        this.aggregateName = aggregateName
        this.aggregateId = aggregateId
        this.sequenceNumber = sequenceNumber
        this.event = event
        this.timestamp = timestamp
    }

    String getEventName() {
        return event.name
    }

    String getSerializedEvent() {
        return GenericEventSerializer.toJSON(event)
    }

    String getSerializedTimestamp() {
        timestamp.format(TIMESTAMP_FORMAT)
    }

    @Override
    String toString() {
        "EventEnvelope[$applicationName.$boundedContextName.$aggregateName{$aggregateId}#$sequenceNumber @${serializedTimestamp} :: <$event>]".toString()
    }

    @Override
    boolean equals(that) {
        this.applicationName == that.applicationName &&
                this.boundedContextName == that.boundedContextName &&
                this.aggregateName == that.aggregateName &&
                this.aggregateId   == that.aggregateId &&
                this.event         == that.event &&
                this.sequenceNumber == that.sequenceNumber &&
                this.timestamp     == that.timestamp
    }

    String toJSON() {
        """{\
"applicationName":"$applicationName",\
"boundedContextName":"$boundedContextName",\
"aggregateName":"$aggregateName",\
"aggregateId":"$aggregateId",\
"eventName":"${event.name}",\
"attributes":${GenericEventSerializer.toJSON(event)},\
"timestamp":"$serializedTimestamp",\
"":""\
}"""
    }

    void applyEventTo(aggregate) {
        event.applyTo(aggregate)
    }
}
