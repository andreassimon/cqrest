package de.oneos.eventstore

import static java.util.UUID.randomUUID

import org.junit.*
import static org.junit.Assert.*
import static org.hamcrest.Matchers.*

import de.oneos.eventsourcing.*


abstract class EventStore_ContractTest {
    static final String APPLICATION_NAME = 'APPLICATION_NAME'
    static final String BOUNDED_CONTEXT_NAME = 'BOUNDED_CONTEXT_NAME'
    static final String AGGREGATE_NAME = 'AGGREGATE_NAME'
    static final UUID   AGGREGATE_ID = randomUUID()

    static final Map<String, Object> VALID_EVENT_ENVELOPE_PROPERTIES = [
            applicationName: APPLICATION_NAME,
            boundedContextName: BOUNDED_CONTEXT_NAME,
            aggregateName: AGGREGATE_NAME,
            aggregateId: AGGREGATE_ID,
            event: new Business_event_happened(),
            sequenceNumber: 0,
            timestamp: new Date()
    ]

    abstract EventStore getEventStore()

    EventEnvelope eventEnvelope

    void setUp() {
        eventEnvelope = new EventEnvelope(
            APPLICATION_NAME,
            BOUNDED_CONTEXT_NAME,
            AGGREGATE_NAME,
            AGGREGATE_ID,
            new Business_event_happened()
        )
    }

    @Test
    void should_provide_UnitOfWork_instances() {
        assertThat eventStore.createUnitOfWork(), notNullValue()
    }

    @Test
    void should_persist_an_EventEnvelope() {
        def eventClassPackageName = eventEnvelope.event.class.package.name

        eventStore.save(eventEnvelope)
        List history = eventStore.loadEvents(
                eventEnvelope.applicationName,
                eventEnvelope.boundedContextName,
                eventEnvelope.aggregateName,
                eventEnvelope.aggregateId
        ) { eventName, eventAttributes ->
            loadEventClass(eventClassPackageName, eventName).newInstance(eventAttributes)
        }

        assertThat history, equalTo([
            eventEnvelope.event
        ])
    }

    Class loadEventClass(eventClassPackageName, eventName) {
        def simpleEventClassName = eventName.replaceAll(' ', '_')
        def fullEventClassName = [eventClassPackageName, simpleEventClassName].join('.')
        this.class.classLoader.loadClass(fullEventClassName)
    }


    @Test(expected = IllegalArgumentException)
    void should_throw_an_exception_when_applicationName_is_null() {
        eventStore.save(validEnvelopeBut(applicationName: null))
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
    void should_throw_an_exception_when_applicationName_is_empty() {
        eventStore.save(validEnvelopeBut(applicationName: ''))
    }

    @Test(expected = IllegalArgumentException)
    void should_throw_an_exception_when_boundedContextName_is_null() {
        eventStore.save(validEnvelopeBut(boundedContextName: null))
    }

    @Test(expected = IllegalArgumentException)
    void should_throw_an_exception_when_boundedContextName_is_empty() {
        eventStore.save(validEnvelopeBut(boundedContextName: ''))
    }

    @Test(expected = IllegalArgumentException)
    void should_throw_an_exception_when_aggregateName_is_null() {
        eventStore.save(validEnvelopeBut(aggregateName: null))
    }

    @Test(expected = IllegalArgumentException)
    void should_throw_an_exception_when_aggregateName_is_empty() {
        eventStore.save(validEnvelopeBut(aggregateName: ''))
    }

    @Test(expected = IllegalArgumentException)
    void should_throw_an_exception_when_aggregateId_is_null() {
        eventStore.save(validEnvelopeBut(aggregateId: null))
    }

    @Test(expected = GroovyRuntimeException)
    void should_throw_an_exception_when_sequenceNumber_is_null() {
        eventStore.save(validEnvelopeBut(sequenceNumber: null))
    }

    @Test(expected = IllegalArgumentException)
    void should_throw_an_exception_when_eventName_is_null() {
        eventStore.save(validEnvelopeBut(event: new Business_event_happened() {
            @Override
            String getName() { null }
        }))
    }

    @Test(expected = IllegalArgumentException)
    void should_throw_an_exception_when_eventName_is_empty() {
        eventStore.save(validEnvelopeBut(event: new Business_event_happened() {
            @Override
            String getName() { '' }
        }))
    }

    @Test(expected = IllegalArgumentException)
    void should_throw_an_exception_when_timestamp_is_null() {
        eventStore.save(validEnvelopeBut(timestamp: null))
    }

    @Test(expected = EventCollisionOccurred)
    void should_throw_an_exception_when_there_is_an_aggregate_event_stream_collision() {
        Map SAME_EVENT_COORDINATES = [
            applicationName: APPLICATION_NAME,
            boundedContextName: BOUNDED_CONTEXT_NAME,
            aggregateName: AGGREGATE_NAME,
            aggregateId: AGGREGATE_ID,
            sequenceNumber: 0
        ]

        eventStore.save(validEnvelopeBut(SAME_EVENT_COORDINATES))
        eventStore.save(validEnvelopeBut(SAME_EVENT_COORDINATES))
    }

    static class Business_event_happened extends Event {
        @Override
        def applyTo(aggregate) { aggregate }
    }

}
