package de.oneos.eventstore

import groovy.json.JsonBuilder
import de.oneos.eventsourcing.Event


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
    final def event

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
        this.event = event
        this.timestamp = timestamp
        this.correlationId = correlationId
        this.user = user
    }

    String getEventName() {
        return event?.eventName
    }

    Map<String, ?> getEventAttributes() {
        return event?.eventAttributes
    }

    String getSerializedEvent() {
        new JsonBuilder(eventAttributes).toString()
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
"eventName":"$event.eventName",\
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

    void applyEventTo(aggregate) {
        aggregate.invokeMethod(event.eventName, event.serializableForm)
    }
}
