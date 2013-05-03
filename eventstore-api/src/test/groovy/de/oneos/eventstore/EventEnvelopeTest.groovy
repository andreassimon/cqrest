package de.oneos.eventstore

import static java.util.UUID.randomUUID
import groovy.json.JsonSlurper

import org.junit.*
import static org.junit.Assert.*
import static org.hamcrest.Matchers.*

import de.oneos.eventsourcing.*


class EventEnvelopeTest {

    final String APPLICATION = 'APPLICATION'
    final String BOUNDED_CONTEXT = 'BOUNDED_CONTEXT'
    final String AGGREGATE = 'AGGREGATE'
    final UUID AGGREGATE_ID = randomUUID()
    final Event EVENT = new Business_event_happened()
    final Integer SEQUENCE_NUMBER = 0
    final Date TIMESTAMP = new Date()
    final UUID CORRELATION_ID = randomUUID()
    final String USER = 'user@company.com'

    JsonSlurper parser = new JsonSlurper()

    EventEnvelope envelope = new EventEnvelope(APPLICATION, BOUNDED_CONTEXT, AGGREGATE, AGGREGATE_ID, EVENT, SEQUENCE_NUMBER, TIMESTAMP, CORRELATION_ID, USER)


    @Test
    void should_serialize_correlationId_to_JSON() {
        envelope.correlationId = CORRELATION_ID

        def deserializedCorrelationId = UUID.fromString(parser.parseText(envelope.toJSON())['correlationId'])

        assertThat deserializedCorrelationId, equalTo(CORRELATION_ID)
    }

    @Test
    void should_export_null_correlationId_as_null_to_JSON() {
        envelope.correlationId = null

        def deserializedCorrelationId = parser.parseText(envelope.toJSON())['correlationId']

        assertThat deserializedCorrelationId, nullValue()
    }

    @Test
    void should_serialize_user_to_JSON() {
        envelope.user = USER

        def deserializedUser = parser.parseText(envelope.toJSON())['user']

        assertThat deserializedUser, equalTo(USER)
    }

    @Test
    void should_export_null_user_as_null_to_JSON() {
        envelope.user = null

        def deserializedUser = parser.parseText(envelope.toJSON())['user']

        assertThat deserializedUser, nullValue()
    }


    static class Business_event_happened extends BaseEvent {
        @Override
        protected List<String> UNSERIALIZED_PROPERTIES() {
            return super.UNSERIALIZED_PROPERTIES() + ['function']
        }

        Closure<Void> function = {}

        @Override
        void applyTo(Object aggregate) {
            function(aggregate)
        }
    }
}
