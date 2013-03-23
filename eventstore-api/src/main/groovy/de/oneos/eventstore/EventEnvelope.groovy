package de.oneos.eventstore

import groovy.json.JsonBuilder
import de.oneos.eventsourcing.Event


class EventEnvelope<AggregateType> {

    static final String NULL = 'null'
    static final String TIMESTAMP_FORMAT = 'yyyy-MM-dd HH:mm:ss.SSS'

    final Date timestamp
    final String applicationName
    final String boundedContextName
    final String aggregateName
    final UUID aggregateId
    final Integer sequenceNumber
    final Event<AggregateType> event

    UUID correlationId
    String user


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
        return event?.eventName
    }

    String getSerializedEvent() {
        new JsonBuilder(event.serializableForm).toString()
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
        this.event         == that.event &&
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
        event.applyTo(aggregate)
    }
}
