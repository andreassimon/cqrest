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
    static UUID NO_CORRELATION_ID = null
    static String USER_UNKNOWN = null

    abstract EventStore getEventStore()

    EventPublisher eventPublisher, defectiveEventPublisher, flawlessEventPublisher
    EventEnvelope expectedEventEnvelope = new EventEnvelope(
        APPLICATION_NAME,
        BOUNDED_CONTEXT_NAME,
        Aggregate.aggregateName,
        AGGREGATE_ID,
        new Business_event_happened(),
        NO_CORRELATION_ID,
        USER_UNKNOWN
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
    }


    @Test
    void should_provide_UnitOfWork_instances() {
        assertThat eventStore.createUnitOfWork(NO_CORRELATION_ID, USER_UNKNOWN), notNullValue()
    }

    @Test
    void should_persist_an_EventEnvelope() {
        eventStore.commit(unitOfWork(eventStream))

        assertThat history(eventStore), equalTo(eventStream)
    }


    @Test
    void should_persist_the_correlationId() {
        UUID correlationId = randomUUID()
        def unitOfWork = new UnitOfWork(eventStore, APPLICATION_NAME, BOUNDED_CONTEXT_NAME, correlationId, USER_UNKNOWN)
        Aggregate aggregate = unitOfWork.attach(new Aggregate(AGGREGATE_ID))
        aggregate.emit(new Business_event_happened())
        eventStore.commit(unitOfWork)

        def envelopes = eventStore.loadEventEnvelopes(AGGREGATE_ID)

        envelopes.each { EventEnvelope envelope ->
            assertThat envelope.correlationId, equalTo(correlationId)
        }
    }

    @Test
    void should_persist_the_user() {
        String user = 'a.simon@quagilis.de'
        def unitOfWork = new UnitOfWork(eventStore, APPLICATION_NAME, BOUNDED_CONTEXT_NAME, NO_CORRELATION_ID, user)
        Aggregate aggregate = unitOfWork.attach(new Aggregate(AGGREGATE_ID))
        aggregate.emit(new Business_event_happened())
        eventStore.commit(unitOfWork)

        def envelopes = eventStore.loadEventEnvelopes(AGGREGATE_ID)

        envelopes.each { EventEnvelope envelope ->
            assertThat envelope.user, equalTo(user)
        }
    }


    protected unitOfWork(Map eventCoordinates = [:], List<Event> events) {
        def unitOfWork = eventStore.createUnitOfWork(NO_CORRELATION_ID, USER_UNKNOWN)
        Aggregate aggregate = new Aggregate(AGGREGATE_ID)
        unitOfWork.attach(aggregate)
        events.each { aggregate.emit(it) }
        unitOfWork
    }

    protected getEventStream() {
        [new Business_event_happened(comment: 'Some very informative words')]
    }

    protected history(EventStore eventStore, aggregateId = AGGREGATE_ID) {
        eventStore.loadEventEnvelopes(aggregateId).collect { it.event }
    }


    @Test(expected = IllegalArgumentException)
    void should_throw_an_exception_when_eventName_is_empty() {
        eventStore.commit(unitOfWork([new Business_event_happened() {
            @Override
            String getEventName() { '' }
        }]))
    }

    @Test(expected = EventCollisionOccurred)
    void should_throw_an_exception_when_there_is_an_aggregate_event_stream_collision() {
        def unitOfWork_1 = eventStore.createUnitOfWork(NO_CORRELATION_ID, USER_UNKNOWN)
        def unitOfWork_2 = eventStore.createUnitOfWork(NO_CORRELATION_ID, USER_UNKNOWN)

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
        def unitOfWork = eventStore.createUnitOfWork(NO_CORRELATION_ID, USER_UNKNOWN)
        unitOfWork.attach(
            new Aggregate(AGGREGATE_ID).emit(new Business_event_happened())
        )
        unitOfWork.attach(
            new Aggregate(ANOTHER_AGGREGATE_ID).emit(new Business_event_happened())
        )

        eventStore.commit(unitOfWork)

        assertThat eventStore.loadEventEnvelopes(AGGREGATE_ID).collect { it.event }, equalTo([
            new Business_event_happened()
        ])
    }

    @Test
    void should_find_EventEnvelopes_by_aggregate_id() {
        def unitOfWork = eventStore.createUnitOfWork(NO_CORRELATION_ID, USER_UNKNOWN)
        unitOfWork.attach(
            new Aggregate(AGGREGATE_ID).emit(new Business_event_happened())
        )
        unitOfWork.attach(
            new Aggregate(ANOTHER_AGGREGATE_ID).emit(new Business_event_happened())
        )

        eventStore.commit(unitOfWork)

        assertThat eventStore.findAll(aggregateId: AGGREGATE_ID).collect { it.event }, equalTo([
            new Business_event_happened()
        ])
    }


    @Test
    void should_not_persist_any_event_if_there_are_collisions_in_UnitOfWork() {
        def unitOfWork_1 = eventStore.createUnitOfWork(NO_CORRELATION_ID, USER_UNKNOWN)
        def unitOfWork_2 = eventStore.createUnitOfWork(NO_CORRELATION_ID, USER_UNKNOWN)

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
        eventStore.inUnitOfWork(NO_CORRELATION_ID, USER_UNKNOWN, {
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

        eventStore.inUnitOfWork NO_CORRELATION_ID, USER_UNKNOWN, publish(expectedEventEnvelope)

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

        eventStore.inUnitOfWork NO_CORRELATION_ID, USER_UNKNOWN, publish(expectedEventEnvelope)

        verify(flawlessEventPublisher).publish(expectedEventEnvelope)
    }

    @Test
    void should_not_publish_any_event_when_transaction_failed() {
        eventStore.publishers = [eventPublisher]
        eventStore.inUnitOfWork NO_CORRELATION_ID, USER_UNKNOWN, publish(expectedEventEnvelope)

        reset(eventPublisher)
        expect(EventCollisionOccurred) {
            eventStore.inUnitOfWork NO_CORRELATION_ID, USER_UNKNOWN, publish(expectedEventEnvelope)
        }

        verify(eventPublisher, never()).publish(expectedEventEnvelope)
    }

    void expect(Class<? extends Throwable> exceptionClass, Closure<Void> block) {
        try {
            block.call()
            throw new AssertionFailedError("Expected $exceptionClass, but none was thrown")
        } catch (exception) {
            if(exception.class != exceptionClass) {
                throw exception
            }
        }
    }

    @Test
    void should_flush_the_UnitOfWork_after_persisting_its_events() {
        int numberOfCalls = 0
        UnitOfWork unitOfWork = new UnitOfWork(eventStore, APPLICATION_NAME, BOUNDED_CONTEXT_NAME, NO_CORRELATION_ID, USER_UNKNOWN) {
            @Override
            void flush() {
                numberOfCalls++
                super.flush()
            }
        }

        eventStore.commit(unitOfWork)

        assertThat numberOfCalls, equalTo(1)
    }


    static class Aggregate {
        static { Aggregate.mixin(EventSourcing) }
        static aggregateName = AGGREGATE_NAME

        final UUID id

        Aggregate(UUID id) { this.id = id }
    }

    static class Business_event_happened extends Event {
        // Having an attribute is important to test deserialization
        String comment

        @Override
        void applyTo(aggregate) { }
    }

}
