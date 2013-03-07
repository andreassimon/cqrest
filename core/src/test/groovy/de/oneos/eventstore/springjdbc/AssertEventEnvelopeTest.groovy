package de.oneos.eventstore.springjdbc

import static java.util.UUID.randomUUID

import org.junit.*

import de.oneos.eventsourcing.*


public class AssertEventEnvelopeTest {

    static UUID AGGREGATE_ID = randomUUID()
    static final Map<String, Object> VALID_EVENT_ENVELOPE_PROPERTIES = [
        applicationName: 'APPLICATION',
        boundedContextName: 'BOUNDED_CONTEXT',
        aggregateName: 'AGGREGATE',
        aggregateId: AGGREGATE_ID,
        event: new Event() { void applyTo(Object t) { } },
        sequenceNumber: 0,
        timestamp: new Date()
    ]


    @Test(expected = IllegalArgumentException)
    void notNull__should_throw_an_exception_when_property_is_null() {
        AssertEventEnvelope.notNull(validEnvelopeBut(applicationName: null), 'applicationName')
    }

    EventEnvelope validEnvelopeBut(Map overriddenProperties) {
        eventEnvelopeWithProperties(VALID_EVENT_ENVELOPE_PROPERTIES + overriddenProperties)
    }

    EventEnvelope eventEnvelopeWithProperties(Map<String, Object> properties) {
        new EventEnvelope(
            properties.applicationName,
            properties.boundedContextName,
            properties.aggregateName,
            properties.aggregateId,
            properties.event,
            properties.sequenceNumber,
            properties.timestamp
        )
    }

    @Test(expected = IllegalArgumentException)
    void notEmpty__should_throw_an_exception_when_property_is_null() {
        AssertEventEnvelope.notEmpty(validEnvelopeBut(applicationName: null), 'applicationName')
    }

    @Test(expected = IllegalArgumentException)
    void notEmpty__should_throw_an_exception_when_property_is_empty() {
        AssertEventEnvelope.notEmpty(validEnvelopeBut(applicationName: ''), 'applicationName')
    }

    @Test
    void notEmpty__should_not_throw_an_exception_when_all_properties_are_valid() {
        AssertEventEnvelope.notEmpty(validEnvelope(), 'applicationName')
    }

    EventEnvelope validEnvelope() {
        eventEnvelopeWithProperties(VALID_EVENT_ENVELOPE_PROPERTIES)
    }

}
