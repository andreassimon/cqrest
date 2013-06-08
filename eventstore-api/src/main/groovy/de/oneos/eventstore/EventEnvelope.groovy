package de.oneos.eventstore

import groovy.json.JsonBuilder


class EventEnvelope<AggregateType> {
    org.apache.commons.logging.Log log = org.apache.commons.logging.LogFactory.getLog(EventEnvelope)

    static final String NULL = 'null'
    static final String TIMESTAMP_FORMAT = 'yyyy-MM-dd HH:mm:ss.SSS'

    final Date timestamp
    final String applicationName
    final String boundedContextName
    final String aggregateName
    final UUID aggregateId
    final Integer sequenceNumber
    final String eventName
    final Map<String, ?> eventAttributes

    UUID correlationId
    String user


    @Deprecated
    EventEnvelope(
        String application,
        String boundedContext,
        String aggregateName,
        UUID aggregateId,
        def event,
        int sequenceNumber = 0,
        Date timestamp = new Date(),
        UUID correlationId,
        String user
    ) {
        assert aggregateName != null

        this.applicationName = application
        this.boundedContextName = boundedContext
        this.aggregateName = aggregateName
        this.aggregateId = aggregateId
        this.sequenceNumber = sequenceNumber
        this.eventName = event.eventName
        this.eventAttributes = event.eventAttributes
        this.timestamp = timestamp
        this.correlationId = correlationId
        this.user = user
    }

    Map<String, ?> getEvent() {
        return [
            eventName: eventName,
            eventAttributes: eventAttributes
        ]
    }

    String getSerializedEvent() {
        new JsonBuilder(eventAttributes).toString()
    }

    String getSerializedTimestamp() {
        timestamp.format(TIMESTAMP_FORMAT)
    }

    @Override
    String toString() {
        "EventEnvelope[$applicationName.$boundedContextName.$aggregateName{$aggregateId}#$sequenceNumber @${serializedTimestamp} :: <$eventName[$serializedEvent]>]".toString()
    }

    @Override
    boolean equals(that) {
        this.class == that.class &&
        this.applicationName == that.applicationName &&
        this.boundedContextName == that.boundedContextName &&
        this.aggregateName == that.aggregateName &&
        this.aggregateId   == that.aggregateId &&
        this.eventName == that.eventName &&
        this.eventAttributes == that.eventAttributes &&
        this.sequenceNumber == that.sequenceNumber
    }

    String toJSON() {
        """{\
"applicationName":"$applicationName",\
"boundedContextName":"$boundedContextName",\
"aggregateName":"$aggregateName",\
"aggregateId":"$aggregateId",\
"eventName":"$eventName",\
"attributes":$serializedEvent,\
"timestamp":"$serializedTimestamp",\
"correlationId":$serializedCorrelationId,\
"user":$serializedUser\
}"""
    }

    String getSerializedCorrelationId() {
        if(correlationId) {
            return "\"$correlationId\""
        }
        NULL
    }

    String getSerializedUser() {
        if(user) {
            return "\"$user\""
        }
        NULL
    }

}
