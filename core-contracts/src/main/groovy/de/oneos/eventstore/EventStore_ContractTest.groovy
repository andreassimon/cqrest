package de.oneos.eventstore

import static java.util.UUID.randomUUID

import org.junit.*
import junit.framework.AssertionFailedError
import static org.junit.Assert.*
import static org.mockito.Mockito.*
import static org.hamcrest.Matchers.*

import de.oneos.eventsourcing.*


abstract class EventStore_ContractTest {
    static final String APPLICATION_NAME = 'APPLICATION_NAME'
    static final String BOUNDED_CONTEXT_NAME = 'BOUNDED_CONTEXT_NAME'
    static final String AGGREGATE_NAME = 'AGGREGATE_NAME'
    static UUID AGGREGATE_ID = randomUUID()
    static UUID ANOTHER_AGGREGATE_ID = randomUUID()

    abstract EventStore getEventStore()

    EventPublisher eventPublisher, defectiveEventPublisher, flawlessEventPublisher
    EventEnvelope expectedEventEnvelope = new EventEnvelope(
        APPLICATION_NAME,
        BOUNDED_CONTEXT_NAME,
        AGGREGATE_NAME,
        AGGREGATE_ID,
        new Business_event_happened()
    )


    Closure<Object> eventFactory

    void setUp() {
        while(ANOTHER_AGGREGATE_ID == AGGREGATE_ID) {
            ANOTHER_AGGREGATE_ID = randomUUID()
        }

        eventPublisher = mock(EventPublisher)

        defectiveEventPublisher = mock(EventPublisher, 'defectiveEventPublisher')
        when(defectiveEventPublisher.publish(any(EventEnvelope))) \
            .thenThrow(new RuntimeException('Thrown by EventPublisher'))

        flawlessEventPublisher = mock(EventPublisher, 'flawlessEventPublisher')

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
    void should_filter_matching_EventEnvelopes_from_history() {
        def unitOfWork = eventStore.createUnitOfWork()
        unitOfWork.publishEvent(APPLICATION_NAME, BOUNDED_CONTEXT_NAME, AGGREGATE_NAME, AGGREGATE_ID, new Business_event_happened())
        unitOfWork.publishEvent(APPLICATION_NAME, BOUNDED_CONTEXT_NAME, AGGREGATE_NAME, ANOTHER_AGGREGATE_ID, new Business_event_happened())
        eventStore.commit(unitOfWork)

        assertThat history(eventStore, AGGREGATE_ID), equalTo([
            new Business_event_happened()
        ])
    }

    @Test
    void should_not_persist_any_event_if_any_in_UnitOfWork_conflicts() {
        def unitOfWork_1 = eventStore.createUnitOfWork()
        def unitOfWork_2 = eventStore.createUnitOfWork()

        unitOfWork_1.publishEvent(APPLICATION_NAME, BOUNDED_CONTEXT_NAME, AGGREGATE_NAME, AGGREGATE_ID, new Business_event_happened())

        unitOfWork_2.publishEvent(APPLICATION_NAME, BOUNDED_CONTEXT_NAME, AGGREGATE_NAME, ANOTHER_AGGREGATE_ID, new Business_event_happened())
        unitOfWork_2.publishEvent(APPLICATION_NAME, BOUNDED_CONTEXT_NAME, AGGREGATE_NAME, AGGREGATE_ID, new Business_event_happened())

        eventStore.commit(unitOfWork_1)
        expect(EventCollisionOccurred) {
            eventStore.commit(unitOfWork_2)
        }

        assertThat history(eventStore, ANOTHER_AGGREGATE_ID), empty()
    }

    @Test
    void should_execute_closure_in_unit_of_work() {
        eventStore.inUnitOfWork {
            publishEvent(APPLICATION_NAME, BOUNDED_CONTEXT_NAME, AGGREGATE_NAME, AGGREGATE_ID, new Business_event_happened())
            publishEvent(APPLICATION_NAME, BOUNDED_CONTEXT_NAME, AGGREGATE_NAME, AGGREGATE_ID, new Business_event_happened())
        }

        assertThat history(eventStore, AGGREGATE_ID), equalTo([
            new Business_event_happened(),
            new Business_event_happened()
        ])
    }

    @Test
    void should_publish_persisted_events_to_registered_EventPublishers() {
        eventStore.publishers = [eventPublisher]

        eventStore.inUnitOfWork publish(expectedEventEnvelope)

        verify(eventPublisher).publish(expectedEventEnvelope)
    }

    protected publish(eventEnvelope) {
        return {
            publishEvent(
                eventEnvelope.applicationName,
                eventEnvelope.boundedContextName,
                eventEnvelope.aggregateName,
                eventEnvelope.aggregateId,
                new Business_event_happened()
            )
        }
    }

    @Test
    void should_ignore_any_exceptions_thrown_by_EventPublisher() {
        eventStore.publishers = [defectiveEventPublisher, flawlessEventPublisher]

        eventStore.inUnitOfWork publish(expectedEventEnvelope)

        verify(flawlessEventPublisher).publish(expectedEventEnvelope)
    }

    @Test
    void should_not_publish_any_event_when_transaction_failed() {
        eventStore.publishers = [eventPublisher]
        eventStore.inUnitOfWork publish(expectedEventEnvelope)

        reset(eventPublisher)
        expect(EventCollisionOccurred) {
            eventStore.inUnitOfWork publish(expectedEventEnvelope)
        }

        verify(eventPublisher, never()).publish(expectedEventEnvelope)
    }

    void expect(Class<Throwable> exceptionClass, Closure<Void> block) {
        try {
            block.call()
            throw new AssertionFailedError("Expected $exceptionClass, but none was thrown")
        } catch (exception) {
            if(exception.class != exceptionClass) {
                throw exception
            }
        }
    }


    static class Business_event_happened extends Event {
        @Override
        void applyTo(aggregate) { }
    }

}
