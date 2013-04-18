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
    static UUID AGGREGATE_ID = randomUUID()
    static UUID ANOTHER_AGGREGATE_ID = randomUUID()
    static UUID NO_CORRELATION_ID = null
    static String USER_UNKNOWN = null

    abstract EventStore getEventStore()

    EventPublisher eventPublisher, defectiveEventPublisher, flawlessEventPublisher
    EventEnvelope expectedEventEnvelope = new EventEnvelope(
        APPLICATION_NAME,
        BOUNDED_CONTEXT_NAME,
        Order.aggregateName,
        AGGREGATE_ID,
        new Order_line_was_added(),
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
        assertThat eventStore.createUnitOfWork(APPLICATION_NAME, BOUNDED_CONTEXT_NAME, NO_CORRELATION_ID, USER_UNKNOWN), notNullValue()
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
        Order aggregate = unitOfWork.attach(new Order(AGGREGATE_ID))
        aggregate.emit(new Order_line_was_added())
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
        Order aggregate = unitOfWork.attach(new Order(AGGREGATE_ID))
        aggregate.emit(new Order_line_was_added())
        eventStore.commit(unitOfWork)

        def envelopes = eventStore.loadEventEnvelopes(AGGREGATE_ID)

        envelopes.each { EventEnvelope envelope ->
            assertThat envelope.user, equalTo(user)
        }
    }


    protected unitOfWork(Map eventCoordinates = [:], List<Event> events) {
        def unitOfWork = eventStore.createUnitOfWork(APPLICATION_NAME, BOUNDED_CONTEXT_NAME, NO_CORRELATION_ID, USER_UNKNOWN)
        Order aggregate = new Order(AGGREGATE_ID)
        unitOfWork.attach(aggregate)
        events.each { aggregate.emit(it) }
        unitOfWork
    }

    protected getEventStream() {
        [new Order_line_was_added(article: 'Teddy Bear')]
    }

    protected history(EventStore eventStore, aggregateId = AGGREGATE_ID) {
        eventStore.loadEventEnvelopes(aggregateId).collect { it.event }
    }


    @Test(expected = IllegalArgumentException)
    void should_throw_an_exception_when_eventName_is_empty() {
        eventStore.commit(unitOfWork([new Order_line_was_added() {
            @Override
            String getEventName() { '' }
        }]))
    }

    @Test(expected = EventCollisionOccurred)
    void should_throw_an_exception_when_there_is_an_aggregate_event_stream_collision() {
        def unitOfWork_1 = eventStore.createUnitOfWork(APPLICATION_NAME, BOUNDED_CONTEXT_NAME, NO_CORRELATION_ID, USER_UNKNOWN)
        def unitOfWork_2 = eventStore.createUnitOfWork(APPLICATION_NAME, BOUNDED_CONTEXT_NAME, NO_CORRELATION_ID, USER_UNKNOWN)

        [unitOfWork_1, unitOfWork_2].each { unitOfWork ->
            Order aggregate = new Order(AGGREGATE_ID)
            unitOfWork.attach(aggregate)
            aggregate.emit(new Order_line_was_added())
        }

        [unitOfWork_1, unitOfWork_2].each { unitOfWork ->
            eventStore.commit(unitOfWork)
        }
    }

    @Test
    void should_filter_matching_EventEnvelopes_from_history() {
        def unitOfWork = eventStore.createUnitOfWork(APPLICATION_NAME, BOUNDED_CONTEXT_NAME, NO_CORRELATION_ID, USER_UNKNOWN)
        unitOfWork.attach(
            new Order(AGGREGATE_ID).emit(new Order_line_was_added())
        )
        unitOfWork.attach(
            new Order(ANOTHER_AGGREGATE_ID).emit(new Order_line_was_added())
        )

        eventStore.commit(unitOfWork)

        assertThat eventStore.loadEventEnvelopes(AGGREGATE_ID).collect { it.event }, equalTo([
            new Order_line_was_added()
        ])
    }

    @Test
    void should_find_EventEnvelopes_by_aggregate_id() {
        def unitOfWork = eventStore.createUnitOfWork(APPLICATION_NAME, BOUNDED_CONTEXT_NAME, NO_CORRELATION_ID, USER_UNKNOWN)
        unitOfWork.attach(new Order(AGGREGATE_ID)).emit(new Order_line_was_added())
        unitOfWork.attach(new Order(ANOTHER_AGGREGATE_ID)).emit(new Order_line_was_added())
        eventStore.commit(unitOfWork)

        def actual = eventStore.findAll(aggregateId: AGGREGATE_ID)

        assertThat actual.collect { it.event }, equalTo([
            new Order_line_was_added()
        ])
    }

    @Test
    void should_find_EventEnvelopes_by_event_name() {
        def unitOfWork = eventStore.createUnitOfWork(APPLICATION_NAME, BOUNDED_CONTEXT_NAME, NO_CORRELATION_ID, USER_UNKNOWN)
        unitOfWork.attach(new Order(AGGREGATE_ID)).emit(new Order_line_was_added())
        unitOfWork.attach(new Order(ANOTHER_AGGREGATE_ID)).emit(new Order_line_was_added())
        eventStore.commit(unitOfWork)

        def actual = eventStore.findAll(eventName: new Order_line_was_added().eventName)

        assertThat actual, equalTo([
            new EventEnvelope(APPLICATION_NAME, BOUNDED_CONTEXT_NAME, Order.aggregateName,         AGGREGATE_ID, new Order_line_was_added(), NO_CORRELATION_ID, USER_UNKNOWN),
            new EventEnvelope(APPLICATION_NAME, BOUNDED_CONTEXT_NAME, Order.aggregateName, ANOTHER_AGGREGATE_ID, new Order_line_was_added(), NO_CORRELATION_ID, USER_UNKNOWN),
        ])
    }

    @Ignore
    @Test
    void should_find_EventEnvelopes_by_aggregate_name() {

    }

    @Test
    void should_not_persist_any_event_if_there_are_collisions_in_UnitOfWork() {
        def unitOfWork_1 = eventStore.createUnitOfWork(APPLICATION_NAME, BOUNDED_CONTEXT_NAME, NO_CORRELATION_ID, USER_UNKNOWN)
        def unitOfWork_2 = eventStore.createUnitOfWork(APPLICATION_NAME, BOUNDED_CONTEXT_NAME, NO_CORRELATION_ID, USER_UNKNOWN)

        unitOfWork_1.with {
            attach(new Order(AGGREGATE_ID).emit(new Order_line_was_added()))
        }

        unitOfWork_2.with {
            attach(new Order(ANOTHER_AGGREGATE_ID).emit(new Order_line_was_added()))
            attach(new Order(AGGREGATE_ID).emit(new Order_line_was_added()))
        }

        eventStore.commit(unitOfWork_1)
        expect(EventCollisionOccurred) {
            eventStore.commit(unitOfWork_2)
        }

        assertThat history(eventStore, ANOTHER_AGGREGATE_ID), empty()
    }

    @Test
    void should_execute_closure_in_unit_of_work() {
        eventStore.inUnitOfWork(APPLICATION_NAME, BOUNDED_CONTEXT_NAME, NO_CORRELATION_ID, USER_UNKNOWN, {
            Order aggregate = new Order(AGGREGATE_ID)
            aggregate.emit(new Order_line_was_added())
            aggregate.emit(new Order_line_was_added())
            attach(aggregate)
        })

        assertThat history(eventStore, AGGREGATE_ID), equalTo([
            new Order_line_was_added(),
            new Order_line_was_added()
        ])
    }

    @Test
    void should_return_the_result_of_the_closure() {
        assertThat eventStore.inUnitOfWork(APPLICATION_NAME, BOUNDED_CONTEXT_NAME, NO_CORRELATION_ID, USER_UNKNOWN) {
            return 'EXPECTED RESULT'
        }, equalTo('EXPECTED RESULT')
    }

    @Test
    void should_publish_persisted_events_to_registered_EventPublishers() {
        eventStore.publishers = [eventPublisher]

        eventStore.inUnitOfWork APPLICATION_NAME, BOUNDED_CONTEXT_NAME, NO_CORRELATION_ID, USER_UNKNOWN, publish(expectedEventEnvelope)

        verify(eventPublisher).publish(expectedEventEnvelope)
    }

    protected publish(EventEnvelope eventEnvelope) {
        assert Order.aggregateName == eventEnvelope.aggregateName
        Order aggregate = new Order(eventEnvelope.aggregateId)
        aggregate.emit(new Order_line_was_added())
        return {
            attach(aggregate)
        }
    }

    @Test
    void should_ignore_any_exceptions_thrown_by_EventPublisher() {
        eventStore.publishers = [defectiveEventPublisher, flawlessEventPublisher]

        eventStore.inUnitOfWork APPLICATION_NAME, BOUNDED_CONTEXT_NAME, NO_CORRELATION_ID, USER_UNKNOWN, publish(expectedEventEnvelope)

        verify(flawlessEventPublisher).publish(expectedEventEnvelope)
    }

    @Test
    void should_not_publish_any_event_when_transaction_failed() {
        eventStore.publishers = [eventPublisher]
        eventStore.inUnitOfWork APPLICATION_NAME, BOUNDED_CONTEXT_NAME, NO_CORRELATION_ID, USER_UNKNOWN, publish(expectedEventEnvelope)

        reset(eventPublisher)
        expect(EventCollisionOccurred) {
            eventStore.inUnitOfWork APPLICATION_NAME, BOUNDED_CONTEXT_NAME, NO_CORRELATION_ID, USER_UNKNOWN, publish(expectedEventEnvelope)
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


    @Aggregate
    static class Order {
        static aggregateName = 'AGGREGATE_NAME'

        final UUID id

        Order(UUID id) { this.id = id }
    }

    static class Order_line_was_added extends BaseEvent<Order> {
        // Having an attribute is important to test deserialization
        String article

        @Override
        void applyTo(Order order) { }
    }

}
