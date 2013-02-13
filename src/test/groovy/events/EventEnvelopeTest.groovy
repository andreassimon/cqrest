package events;

import org.junit.Test

import static org.junit.Assert.assertThat
import static org.hamcrest.Matchers.equalTo
import static java.util.UUID.randomUUID
import oneos.test.domain.events.Device_was_registered
import domain.events.Event
import domain.events.EventEnvelope
import groovy.json.JsonSlurper;

public class EventEnvelopeTest {
    String applicationName = 'CQRS Core Library'
    String boundedContextName = 'Tests'
    String aggregateName = 'Aggregate'
    UUID aggregateId = randomUUID()
    Event event = new Device_was_registered(deviceId: aggregateId, deviceName: 'a device')
    Date timestamp = new Date()

    JsonSlurper jsonSlurper = new JsonSlurper()


    @Test
    public void toString__should_produce_sensible_output() {
        EventEnvelope envelope = new EventEnvelope(applicationName, boundedContextName, aggregateName, aggregateId, event, timestamp)

        assertThat envelope.toString(), equalTo("EventEnvelope[$applicationName.$boundedContextName.$aggregateName{$aggregateId} @${timestamp.format('yyyy-MM-dd HH:mm:ss.SSS')} :: <$event>]".toString())
    }

    @Test
    void toJSON__should_serialize_metadata_and_event_attributes() {
        EventEnvelope envelope = new EventEnvelope(applicationName, boundedContextName, aggregateName, aggregateId, event, timestamp)

        String json = envelope.toJSON()

        def parsedJSON = jsonSlurper.parseText(json)
        assertThat(parsedJSON.applicationName, equalTo(applicationName))
        assertThat(parsedJSON.boundedContextName, equalTo(boundedContextName))
        assertThat(parsedJSON.aggregateName, equalTo(aggregateName))
        assertThat(parsedJSON.aggregateId, equalTo(aggregateId.toString()))
        assertThat(parsedJSON.eventName, equalTo(event.name))
        assertThat(parsedJSON.attributes, equalTo(event.attributes()))
        assertThat(parsedJSON.timestamp, equalTo(timestamp.format(EventEnvelope.TIMESTAMP_FORMAT)))
    }

    @Test
    public void equals__should_be_true_for_envelopes_with_equal_attributes() {
        EventEnvelope envelopeA = new EventEnvelope(applicationName, boundedContextName, aggregateName, aggregateId, event, timestamp)
        EventEnvelope envelopeB = new EventEnvelope(applicationName, boundedContextName, aggregateName, aggregateId, event, timestamp)

        assertThat envelopeA, equalTo(envelopeB)
    }
}
