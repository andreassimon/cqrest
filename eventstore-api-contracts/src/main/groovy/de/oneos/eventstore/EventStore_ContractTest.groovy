package de.oneos.eventstore

import static java.util.UUID.randomUUID

import org.junit.*
import junit.framework.AssertionFailedError
import static org.junit.Assert.*
import static org.mockito.Mockito.*
import static org.hamcrest.Matchers.*

import de.oneos.eventsourcing.*

abstract class EventStore_ContractTest {
    static final String APPLICATION_NAME = 'APPLICATION'
    static final String BOUNDED_CONTEXT_NAME = 'BOUNDED CONTEXT'
    static UUID ORDER_ID = UUID.fromString('836ed0d1-e87f-4d70-80f3-7aa44d00ed5d')
    static UUID ANOTHER_ORDER_ID = UUID.fromString('92bc8efe-0f5e-42f7-8dd6-3029d2d1a4eb')
    static UUID CUSTOMER_ID = UUID.fromString('3faeb1ad-6d46-458e-9b77-ee3a6e0ff3ce')
    static UUID NO_CORRELATION_ID = null
    static String USER_UNKNOWN = null

    abstract EventStore getEventStore()

    EventPublisher eventPublisher, defectiveEventPublisher, flawlessEventPublisher
    EventEnvelope expectedEventEnvelope = new EventEnvelope(
        APPLICATION_NAME,
        BOUNDED_CONTEXT_NAME,
        Order.aggregateName,
        ORDER_ID,
        orderLineWasAdded(),
        NO_CORRELATION_ID,
        USER_UNKNOWN
    )


    Closure<Object> eventFactory

    void setUp() {
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

        assertThat history(eventStore), equalTo(eventStream.collect { distill(it) })
    }


    @Test
    void should_persist_the_correlationId() {
        UUID correlationId = randomUUID()
        def unitOfWork = new UnitOfWork(eventStore, APPLICATION_NAME, BOUNDED_CONTEXT_NAME, correlationId, USER_UNKNOWN)
        Order aggregate = unitOfWork.attach(new Order(ORDER_ID))
        aggregate.emit(orderLineWasAdded())
        eventStore.commit(unitOfWork)

        def envelopes = eventStore.loadEventEnvelopes(ORDER_ID)

        envelopes.each { EventEnvelope envelope ->
            assertThat envelope.correlationId, equalTo(correlationId)
        }
    }

    @Test
    void should_persist_the_user() {
        String user = 'a.simon@quagilis.de'
        def unitOfWork = new UnitOfWork(eventStore, APPLICATION_NAME, BOUNDED_CONTEXT_NAME, NO_CORRELATION_ID, user)
        Order aggregate = unitOfWork.attach(new Order(ORDER_ID))
        aggregate.emit(orderLineWasAdded())
        eventStore.commit(unitOfWork)

        def envelopes = eventStore.loadEventEnvelopes(ORDER_ID)

        envelopes.each { EventEnvelope envelope ->
            assertThat envelope.user, equalTo(user)
        }
    }


    protected unitOfWork(Map eventCoordinates = [:], List<Event> events) {
        def unitOfWork = createUnitOfWork()
        Order aggregate = new Order(ORDER_ID)
        unitOfWork.attach(aggregate)
        events.each { aggregate.emit(it) }
        unitOfWork
    }

    protected getEventStream() {
        [new Order_line_was_added(article: 'Teddy Bear')]
    }

    protected history(EventStore eventStore, aggregateId = ORDER_ID) {
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
            Order aggregate = new Order(ORDER_ID)
            unitOfWork.attach(aggregate)
            aggregate.emit(orderLineWasAdded())
        }

        [unitOfWork_1, unitOfWork_2].each { unitOfWork ->
            eventStore.commit(unitOfWork)
        }
    }

    @Test
    void should_filter_matching_EventEnvelopes_from_history() {
        def unitOfWork = createUnitOfWork()
        unitOfWork.attach(
            new Order(ORDER_ID).emit(orderLineWasAdded())
        )
        unitOfWork.attach(
            new Order(ANOTHER_ORDER_ID).emit(orderLineWasAdded())
        )

        eventStore.commit(unitOfWork)

        assertThat eventStore.loadEventEnvelopes(ORDER_ID).collect { it.event }, equalTo([
            distill(orderLineWasAdded())
        ])
    }

    @Test
    void should_find_EventEnvelopes_by_aggregate_id() {
        def unitOfWork = createUnitOfWork()
        unitOfWork.attach(new Order(ORDER_ID)).emit(orderLineWasAdded())
        unitOfWork.attach(new Order(ANOTHER_ORDER_ID)).emit(orderLineWasAdded())
        eventStore.commit(unitOfWork)

        def actual = eventStore.findAll(aggregateId: ORDER_ID)

        assertThat actual.collect { it.event }, equalTo([
            distill(orderLineWasAdded())
        ])
    }

    @Test
    void should_find_EventEnvelopes_by_event_name() {
        def unitOfWork = createUnitOfWork()
        unitOfWork.attach(new Order(ORDER_ID)).emit(orderLineWasAdded())
        unitOfWork.attach(new Order(ANOTHER_ORDER_ID)).emit(orderLineWasAdded())
        eventStore.commit(unitOfWork)

        def actual = eventStore.findAll(eventName: orderLineWasAdded().eventName)

        assertThat actual, equalTo([
            new EventEnvelope(APPLICATION_NAME, BOUNDED_CONTEXT_NAME, Order.aggregateName,         ORDER_ID, orderLineWasAdded(), NO_CORRELATION_ID, USER_UNKNOWN),
            new EventEnvelope(APPLICATION_NAME, BOUNDED_CONTEXT_NAME, Order.aggregateName, ANOTHER_ORDER_ID, orderLineWasAdded(), NO_CORRELATION_ID, USER_UNKNOWN),
        ])
    }

    @Test
    void should_find_EventEnvelopes_by_multiple_event_names() {
        def unitOfWork = createUnitOfWork()
        unitOfWork.attach(new Order(ORDER_ID)).emit(orderLineWasAdded())
        unitOfWork.attach(new Order(ANOTHER_ORDER_ID)).emit(orderLineWasRemoved())
        eventStore.commit(unitOfWork)

        def actual = eventStore.findAll(eventName: [orderLineWasAdded().eventName, orderLineWasRemoved().eventName])

        assertThat actual, equalTo([
            new EventEnvelope(APPLICATION_NAME, BOUNDED_CONTEXT_NAME, Order.aggregateName,         ORDER_ID, orderLineWasAdded(),   NO_CORRELATION_ID, USER_UNKNOWN),
            new EventEnvelope(APPLICATION_NAME, BOUNDED_CONTEXT_NAME, Order.aggregateName, ANOTHER_ORDER_ID, orderLineWasRemoved(), NO_CORRELATION_ID, USER_UNKNOWN),
        ])
    }

    @Test
    void should_order_events_by_sequence_number() {
        def unitOfWork = createUnitOfWork()
        unitOfWork.attach(new Order(ORDER_ID)).
            emit(orderLineWasAdded()).
            emit(orderLineWasRemoved()).
            emit(orderLineWasAdded())
        eventStore.commit(unitOfWork)

        def actual = eventStore.findAll(eventName: [orderLineWasAdded().eventName, orderLineWasRemoved().eventName])

        assertThat actual.collect { it.sequenceNumber }, equalTo([ 0, 1, 2 ])
    }

    protected static Order_line_was_added orderLineWasAdded() {
        new Order_line_was_added()
    }

    protected static Order_line_was_removed orderLineWasRemoved() {
        new Order_line_was_removed()
    }

    @Test
    void should_find_EventEnvelopes_by_aggregate_name() {
        UnitOfWork unitOfWork = createUnitOfWork()
        unitOfWork.attach(new Order(ORDER_ID)).emit(orderLineWasAdded())
        unitOfWork.attach(new Order(ANOTHER_ORDER_ID)).emit(orderLineWasAdded())
        unitOfWork.attach(new Customer(CUSTOMER_ID)).emit(premium_state_assigned())
        eventStore.commit(unitOfWork)

        def actual = eventStore.findAll(aggregateName: Order.aggregateName)

        assertThat actual, equalTo([
            new EventEnvelope(APPLICATION_NAME, BOUNDED_CONTEXT_NAME, Order.aggregateName,         ORDER_ID, [ eventName: orderLineWasAdded().eventName, eventAttributes: orderLineWasAdded().eventAttributes], NO_CORRELATION_ID, USER_UNKNOWN),
            new EventEnvelope(APPLICATION_NAME, BOUNDED_CONTEXT_NAME, Order.aggregateName, ANOTHER_ORDER_ID, [ eventName: orderLineWasAdded().eventName, eventAttributes: orderLineWasAdded().eventAttributes], NO_CORRELATION_ID, USER_UNKNOWN),
        ])
    }

    protected UnitOfWork createUnitOfWork() {
        eventStore.createUnitOfWork(APPLICATION_NAME, BOUNDED_CONTEXT_NAME, NO_CORRELATION_ID, USER_UNKNOWN)
    }

    @Test
    void should_not_persist_any_event_if_there_are_collisions_in_UnitOfWork() {
        def unitOfWork_1 = eventStore.createUnitOfWork(APPLICATION_NAME, BOUNDED_CONTEXT_NAME, NO_CORRELATION_ID, USER_UNKNOWN)
        def unitOfWork_2 = eventStore.createUnitOfWork(APPLICATION_NAME, BOUNDED_CONTEXT_NAME, NO_CORRELATION_ID, USER_UNKNOWN)

        unitOfWork_1.with {
            attach(new Order(ORDER_ID).emit(orderLineWasAdded()))
        }

        unitOfWork_2.with {
            attach(new Order(ANOTHER_ORDER_ID).emit(orderLineWasAdded()))
            attach(new Order(ORDER_ID).emit(orderLineWasAdded()))
        }

        eventStore.commit(unitOfWork_1)
        expect(EventCollisionOccurred) {
            eventStore.commit(unitOfWork_2)
        }

        assertThat history(eventStore, ANOTHER_ORDER_ID), empty()
    }

    @Test
    void should_execute_closure_in_unit_of_work() {
        eventStore.inUnitOfWork(APPLICATION_NAME, BOUNDED_CONTEXT_NAME, NO_CORRELATION_ID, USER_UNKNOWN, {
            Order aggregate = new Order(ORDER_ID)
            aggregate.emit(orderLineWasAdded())
            aggregate.emit(orderLineWasAdded())
            attach(aggregate)
        })

        assertThat history(eventStore, ORDER_ID), equalTo([
            distill(orderLineWasAdded()),
            distill(orderLineWasAdded())
        ])
    }

    Map<String, ?> distill(event) {
        return [
            eventName:  event['eventName'],
            eventAttributes:  event['eventAttributes']
        ]
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
        aggregate.emit(orderLineWasAdded())
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
        static aggregateName = 'Order'

        final UUID id

        Order(UUID id) { this.id = id }

        def "Order line was added"(Map event) { }
        def "Order line was removed"(Map event) { }
    }

    static class Order_line_was_added extends BaseEvent<Order> {
        // Having an attribute is important to test deserialization
        String article
    }

    static class Order_line_was_removed extends BaseEvent<Order> {
        // Having an attribute is important to test deserialization
        String article
    }


    @Aggregate
    static class Customer {
        static aggregateName = 'Customer'

        final UUID id

        Customer(UUID id) { this.id = id }

        def "Customer was assigned premium state"(Map event) { }

    }

    static Customer_was_assigned_premium_state premium_state_assigned() {
        return new Customer_was_assigned_premium_state()
    }

    static class Customer_was_assigned_premium_state extends BaseEvent<Customer> { }

}
