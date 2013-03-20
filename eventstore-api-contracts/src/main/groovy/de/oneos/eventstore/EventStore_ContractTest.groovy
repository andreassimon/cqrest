package de.oneos.eventstore

import static java.util.UUID.randomUUID

import org.junit.*
import junit.framework.AssertionFailedError
import static org.junit.Assert.*
import static org.mockito.Mockito.*
import static org.hamcrest.Matchers.*

import de.oneos.eventsourcing.*


// TODO Must persist correlationId and user(name) attributes
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
        Aggregate.aggregateName,
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
        doThrow(new RuntimeException('Thrown by EventPublisher')).
            when(defectiveEventPublisher).publish(any(EventEnvelope))

        flawlessEventPublisher = mock(EventPublisher, 'flawlessEventPublisher')

        // TODO Try to get rid of it
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
        Aggregate aggregate = new Aggregate(AGGREGATE_ID)
        unitOfWork.attach(aggregate)
        events.each { aggregate.emit(it) }
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
            Aggregate aggregate = new Aggregate(AGGREGATE_ID)
            unitOfWork.attach(aggregate)
            aggregate.emit(new Business_event_happened())
        }

        [unitOfWork_1, unitOfWork_2].each { unitOfWork ->
            eventStore.commit(unitOfWork)
        }
    }

    @Test
    void should_filter_matching_EventEnvelopes_from_history() {
        def unitOfWork = eventStore.createUnitOfWork()
        unitOfWork.attach(
            new Aggregate(AGGREGATE_ID).emit(new Business_event_happened())
        )
        unitOfWork.attach(
            new Aggregate(ANOTHER_AGGREGATE_ID).emit(new Business_event_happened())
        )

        eventStore.commit(unitOfWork)

        assertThat history(eventStore, AGGREGATE_ID), equalTo([
            new Business_event_happened()
        ])
    }

    @Test
    void should_not_persist_any_event_if_any_in_UnitOfWork_conflicts() {
        def unitOfWork_1 = eventStore.createUnitOfWork()
        def unitOfWork_2 = eventStore.createUnitOfWork()

        unitOfWork_1.with {
            attach(new Aggregate(AGGREGATE_ID).emit(new Business_event_happened()))
        }

        unitOfWork_2.with {
            attach(new Aggregate(ANOTHER_AGGREGATE_ID).emit(new Business_event_happened()))
            attach(new Aggregate(AGGREGATE_ID).emit(new Business_event_happened()))
        }

        eventStore.commit(unitOfWork_1)
        expect(EventCollisionOccurred) {
            eventStore.commit(unitOfWork_2)
        }

        assertThat history(eventStore, ANOTHER_AGGREGATE_ID), empty()
    }

    @Test
    void should_execute_closure_in_unit_of_work() {
        eventStore.inUnitOfWork({
            Aggregate aggregate = new Aggregate(AGGREGATE_ID)
            aggregate.emit(new Business_event_happened())
            aggregate.emit(new Business_event_happened())
            attach(aggregate)
        })

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

    protected publish(EventEnvelope eventEnvelope) {
        assert Aggregate.aggregateName == eventEnvelope.aggregateName
        Aggregate aggregate = new Aggregate(eventEnvelope.aggregateId)
        aggregate.emit(new Business_event_happened())
        return {
            attach(aggregate)
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


    static class Aggregate {
        static { Aggregate.mixin(EventSourcing) }
        static aggregateName = AGGREGATE_NAME

        final UUID id

        Aggregate(UUID id) { this.id = id }
    }

    static class Business_event_happened extends Event {
        @Override
        void applyTo(aggregate) { }
    }

}
