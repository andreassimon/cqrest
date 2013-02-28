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
    static UUID AGGREGATE_ID = randomUUID()
    static UUID ANOTHER_AGGREGATE_ID = randomUUID()

    abstract EventStore getEventStore()

    Closure<Object> eventFactory

    void setUp() {
        while(ANOTHER_AGGREGATE_ID == AGGREGATE_ID) {
            ANOTHER_AGGREGATE_ID = randomUUID()
        }
        eventFactory = { classLoader, packageName, eventName, eventAttributes ->
            def simpleEventClassName = eventName.replaceAll(' ', '_')
            def fullEventClassName = [packageName, simpleEventClassName].join('.')
            classLoader.loadClass(fullEventClassName).newInstance(eventAttributes)
        }.curry(Business_event_happened.classLoader, Business_event_happened.package.name)
    }


    @Test
    void should_provide_UnitOfWork_instances() {
        assertThat eventStore.createUnitOfWork(), notNullValue()
    }

    @Test
    void should_persist_an_EventEnvelope() {
        eventStore.commit(unitOfWork(eventStream))

        assertThat history(eventStore), equalTo(eventStream)
    }

    protected unitOfWork(Map eventCoordinates = [:], List<Event> events) {
        def unitOfWork = eventStore.createUnitOfWork()
        events.each { event ->
            unitOfWork.publishEvent(
                eventCoordinates.containsKey('applicationName') ? eventCoordinates['applicationName'] : APPLICATION_NAME,
                eventCoordinates.containsKey('boundedContextName') ? eventCoordinates['boundedContextName'] : BOUNDED_CONTEXT_NAME,
                eventCoordinates.containsKey('aggregateName') ? eventCoordinates['aggregateName'] : AGGREGATE_NAME,
                eventCoordinates.containsKey('aggregateId') ? eventCoordinates['aggregateId'] : AGGREGATE_ID,
                event
            )
        }
        unitOfWork
    }

    protected getEventStream() {
        [new Business_event_happened()]
    }

    protected history(EventStore eventStore, aggregateId = AGGREGATE_ID) {
        eventStore.loadEventEnvelopes(
            APPLICATION_NAME,
            BOUNDED_CONTEXT_NAME,
            AGGREGATE_NAME,
            aggregateId,
            eventFactory
        ).collect { it.event }
    }


    @Test(expected = IllegalArgumentException)
    void should_throw_an_exception_when_applicationName_is_empty() {
        eventStore.commit(unitOfWork(eventStream, applicationName: ''))
    }

    @Test(expected = IllegalArgumentException)
    void should_throw_an_exception_when_boundedContextName_is_empty() {
        eventStore.commit(unitOfWork(eventStream, boundedContextName: ''))
    }

    @Test(expected = IllegalArgumentException)
    void should_throw_an_exception_when_aggregateName_is_empty() {
        eventStore.commit(unitOfWork(eventStream, aggregateName: ''))
    }

    @Test(expected = IllegalArgumentException)
    void should_throw_an_exception_when_aggregateId_is_null() {
        eventStore.commit(unitOfWork(eventStream, aggregateId: null))
    }

    @Test(expected = IllegalArgumentException)
    void should_throw_an_exception_when_eventName_is_empty() {
        eventStore.commit(unitOfWork([new Business_event_happened() {
            @Override
            String getName() { '' }
        }]))
    }

    @Test(expected = EventCollisionOccurred)
    void should_throw_an_exception_when_there_is_an_aggregate_event_stream_collision() {
        def unitOfWork_1 = eventStore.createUnitOfWork()
        def unitOfWork_2 = eventStore.createUnitOfWork()

        [unitOfWork_1, unitOfWork_2].each { unitOfWork ->
            unitOfWork.publishEvent(APPLICATION_NAME, BOUNDED_CONTEXT_NAME, AGGREGATE_NAME, AGGREGATE_ID, new Business_event_happened())
        }

        [unitOfWork_1, unitOfWork_2].each { unitOfWork ->
            eventStore.commit(unitOfWork)
        }
    }

    @Test
    void should_not_persist_any_event_if_any_in_UnitOfWork_conflicts() {
        def unitOfWork_1 = eventStore.createUnitOfWork()
        def unitOfWork_2 = eventStore.createUnitOfWork()

        unitOfWork_1.publishEvent(APPLICATION_NAME, BOUNDED_CONTEXT_NAME, AGGREGATE_NAME, AGGREGATE_ID, new Business_event_happened())

        unitOfWork_2.publishEvent(APPLICATION_NAME, BOUNDED_CONTEXT_NAME, AGGREGATE_NAME, ANOTHER_AGGREGATE_ID, new Business_event_happened())
        unitOfWork_2.publishEvent(APPLICATION_NAME, BOUNDED_CONTEXT_NAME, AGGREGATE_NAME, AGGREGATE_ID, new Business_event_happened())

        eventStore.commit(unitOfWork_1)
        try {
            eventStore.commit(unitOfWork_2)
        } catch (EventCollisionOccurred) { }

        assertThat history(eventStore, ANOTHER_AGGREGATE_ID), empty()
    }

    static class Business_event_happened extends Event {
        @Override
        void applyTo(aggregate) { }
    }

}
