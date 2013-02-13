package domain.events;

import org.junit.Test

import static org.junit.Assert.assertThat
import static org.hamcrest.Matchers.equalTo
import static java.util.UUID.randomUUID;

public class EventEnvelopeTest {
    String applicationName = 'CQRS Core Library'
    String boundedContextName = 'Tests'
    String aggregateName = 'Aggregate'
    UUID aggregateId = randomUUID()
    Event event = new Device_was_registered(deviceId: aggregateId, deviceName: 'a device')
    Date timestamp = new Date()

    @Test
    public void toString__should_produce_sensible_output() {
        EventEnvelope envelopeA = new EventEnvelope(applicationName, boundedContextName, aggregateName, aggregateId, event, timestamp)

        assertThat envelopeA.toString(), equalTo("EventEnvelope[$applicationName.$boundedContextName.$aggregateName{$aggregateId} @${timestamp.format('yyyy-MM-dd HH:mm:ss.SSS')} :: <$event>]".toString())
    }


    @Test
    public void equals__should_be_true_for_envelopes_with_equal_attributes() {
        EventEnvelope envelopeA = new EventEnvelope(applicationName, boundedContextName, aggregateName, aggregateId, event, timestamp)
        EventEnvelope envelopeB = new EventEnvelope(applicationName, boundedContextName, aggregateName, aggregateId, event, timestamp)

        assertThat envelopeA, equalTo(envelopeB)
    }
}
