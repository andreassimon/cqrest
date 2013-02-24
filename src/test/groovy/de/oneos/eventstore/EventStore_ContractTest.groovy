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

    abstract EventStore getEventStore()

    EventEnvelope eventEnvelope = new EventEnvelope(
        APPLICATION_NAME,
        BOUNDED_CONTEXT_NAME,
        AGGREGATE_NAME,
        AGGREGATE_ID,
        new Business_event_happened()
    )

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


    static class Business_event_happened extends Event {
        @Override
        def applyTo(aggregate) { aggregate }
    }

}
