package de.oneos.eventstore

import static java.util.UUID.randomUUID
import java.util.concurrent.CountDownLatch

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

    @Test(timeout = 1000L)
    void should_parse_timestamps() {
        final samples = [
          '2013-04-15 11:25:31.000',
          '2013-05-29 11:29:23.000',
          '2013-08-25 05:58:52.000',
          '2013-08-31 11:01:30.000',
          '2013-10-19 12:39:29.000'
        ]
        CountDownLatch latch = new CountDownLatch(samples.size() * faculty(samples.size()))
        samples.permutations().each { perm ->
                Thread.start {
                    perm.each {
                        latch.countDown()
                        EventEnvelope.parseTimestamp([timestamp: it])
                    }
                }
            }
        latch.await()
    }

    public int faculty(int n) {
        assert n > 0
        (1..n).inject { int a, int b -> a * b}
    }


    static class Business_event_happened extends BaseEvent { }

}
