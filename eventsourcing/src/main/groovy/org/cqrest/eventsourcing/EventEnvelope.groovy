package org.cqrest.eventsourcing

import groovy.json.*

import java.text.SimpleDateFormat

import org.apache.commons.logging.Log
import org.apache.commons.logging.LogFactory


class EventEnvelope {
    public static Log log = LogFactory.getLog(EventEnvelope)

    static final String NULL = 'null'
    static final SimpleDateFormat TIMESTAMP_FORMAT = new SimpleDateFormat('yyyy-MM-dd HH:mm:ss.SSS')

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


    EventEnvelope(
        String application,
        String boundedContext,
        String aggregateName,
        UUID aggregateId,
        Map<String, ?> event,
        int sequenceNumber = 0,
        Date timestamp = new Date(),
        UUID correlationId,
        String user
    ) {
        this(
          application,
          boundedContext,
          aggregateName,
          aggregateId,
          event.eventName as String,
          event.eventAttributes as Map<String, ?>,
          sequenceNumber,
          timestamp,
          correlationId,
          user
        )
    }

    @Deprecated
    EventEnvelope(
      String application,
      String boundedContext,
      String aggregateName,
      UUID aggregateId,
      Event<?> event,
      int sequenceNumber = 0,
      Date timestamp = new Date(),
      UUID correlationId,
      String user
    ) {
        this(
          application,
          boundedContext,
          aggregateName,
          aggregateId,
          event.eventName,
          event.eventAttributes,
          sequenceNumber,
          timestamp,
          correlationId,
          user
        )
    }

    EventEnvelope(
      String application,
      String boundedContext,
      String aggregateName,
      UUID aggregateId,
      String eventName,
      Map<String, ?> eventAttributes,
      int sequenceNumber,
      Date timestamp = new Date(),
      UUID correlationId,
      String user
    ) {
        assert aggregateName != null
        assert null != timestamp

        this.applicationName = application
        this.boundedContextName = boundedContext
        this.aggregateName = aggregateName
        this.aggregateId = aggregateId
        this.sequenceNumber = sequenceNumber
        this.eventName = eventName
        this.eventAttributes = eventAttributes
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
        synchronized(TIMESTAMP_FORMAT) {
            return TIMESTAMP_FORMAT.format(timestamp)
        }
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
"sequenceNumber":$sequenceNumber,\
"timestamp":"$serializedTimestamp",\
"correlationId":$serializedCorrelationId,\
"user":$serializedUser\
}"""
    }

    // TODO test
    static EventEnvelope fromJSON(String json) {
        Map<String, ?> attributes = new JsonSlurper().parseText(json)
        return new EventEnvelope(
            attributes['applicationName'],
            attributes['boundedContextName'],
            attributes['aggregateName'],
            UUID.fromString(attributes['aggregateId']),
            [
                eventName: attributes['eventName'],
                eventAttributes: attributes['attributes'],
            ],
            attributes['sequenceNumber'] as int,
            parseTimestamp(attributes),
            correlationId(attributes),
            attributes['user']
        )
    }

    static Date parseTimestamp(Map<String, ?> attributes) throws IllegalArgumentException, RuntimeException {
        if(!attributes.containsKey('timestamp')) {
            throw new IllegalArgumentException("Event attributes '$attributes' must contain a timestamp!")
        }
        if(attributes['timestamp'].empty) {
            throw new IllegalArgumentException("Event timestamp must not be empty!")
        }
        synchronized(TIMESTAMP_FORMAT) {
            try {
                return TIMESTAMP_FORMAT.parse(attributes['timestamp'] as String)
            } catch(NumberFormatException|ArrayIndexOutOfBoundsException e) {
                throw new RuntimeException("Timestamp `${attributes['timestamp']}` could not be parsed with `${TIMESTAMP_FORMAT.toPattern()}`", e)
            }
        }
    }

    protected static UUID correlationId(Map<String, ?> attributes) {
        if(attributes['correlationId']) {
            return UUID.fromString(attributes['correlationId'] as String)
        }
        return null
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
