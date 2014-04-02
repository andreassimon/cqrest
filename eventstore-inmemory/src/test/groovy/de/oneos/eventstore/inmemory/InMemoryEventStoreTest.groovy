package de.oneos.eventstore.inmemory

import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit

import org.junit.*
import static org.junit.Assert.fail

import org.cqrest.reactive.test.MockObserver

import de.oneos.eventsourcing.EventEnvelope
import de.oneos.eventstore.EventStore_ContractTest


class InMemoryEventStoreTest extends EventStore_ContractTest {

    InMemoryEventStore eventStore

    @Before
    public void setUp() {
        super.setUp()

        eventStore = new InMemoryEventStore()
    }

    @Test
    void observe__should_send_EventEnvelopes_to_subscribed_Observers() {
        eventStore.history = [
          new EventEnvelope(APPLICATION_NAME, BOUNDED_CONTEXT_NAME, EventStore_ContractTest.Order.aggregateName, ORDER_ID, orderLineWasAdded(), NO_CORRELATION_ID, USER_UNKNOWN),
          new EventEnvelope(APPLICATION_NAME, BOUNDED_CONTEXT_NAME, EventStore_ContractTest.Order.aggregateName, ANOTHER_ORDER_ID, orderLineWasAdded(), NO_CORRELATION_ID, USER_UNKNOWN),
        ]

        final Iterator<EventEnvelope> expectedEnvelopes = eventStore.history.iterator()
        CountDownLatch latch = new CountDownLatch(eventStore.history.size())

        eventStore.observe([:]).subscribe([
          onNext: { nextEnvelope ->
              assert expectedEnvelopes.next() == nextEnvelope
              latch.countDown()
          }
        ] as org.cqrest.reactive.Observer<EventEnvelope>)

        if(!latch.await(500L, TimeUnit.MILLISECONDS)) {
            fail("The expected items were not passed within timeout limit")
        }
    }

    @Test
    void observe__should_filter_EventEnvelopes_by_event_names() {
        final List<EventEnvelope> notMatching = [
          new EventEnvelope(APPLICATION_NAME, BOUNDED_CONTEXT_NAME, EventStore_ContractTest.Order.aggregateName, ORDER_ID, orderLineWasAdded(), NO_CORRELATION_ID, USER_UNKNOWN)
        ]
        final List<EventEnvelope> matching = [
          new EventEnvelope(APPLICATION_NAME, BOUNDED_CONTEXT_NAME, EventStore_ContractTest.Order.aggregateName, ANOTHER_ORDER_ID, orderLineWasRemoved(), NO_CORRELATION_ID, USER_UNKNOWN),
        ]
        eventStore.history = notMatching + matching
        final MockObserver observer = new MockObserver(matching)

        eventStore.observe(eventName: [orderLineWasRemoved().eventName]).subscribe(observer)

        observer.assertReceivedEvents()
    }


    // TODO test closing subscriptions

    @Test
    @Ignore
    void should_pass_new_persisted_events_to_subscribed_Observers() {
        final List<EventEnvelope> newEvents = [expectedEventEnvelope]
        final MockObserver observer = new MockObserver(newEvents)
        eventStore.observe([:]).subscribe(observer)

        eventStore.saveEnvelopes(newEvents)

        observer.assertReceivedEvents()
    }

    @Ignore
    @Test
    void should_ignore_any_exceptions_thrown_by_EventConsumer() {
//        [defectiveEventConsumer, flawlessEventConsumer].each {
//            eventStore.subscribeTo([:], it)
//        }
//
//        eventStore.inUnitOfWork APPLICATION_NAME, BOUNDED_CONTEXT_NAME, NO_CORRELATION_ID, USER_UNKNOWN, publish(expectedEventEnvelope)
//
//        verify(flawlessEventConsumer).process(expectedEventEnvelope)
    }

    @Ignore
    @Test
    void should_not_publish_any_event_when_transaction_failed() {
//        eventStore.subscribeTo([:], mockEventConsumer)
//        eventStore.inUnitOfWork APPLICATION_NAME, BOUNDED_CONTEXT_NAME, NO_CORRELATION_ID, USER_UNKNOWN, publish(expectedEventEnvelope)
//
//        reset(mockEventConsumer)
//        expect(EventCollisionOccurred) {
//            eventStore.inUnitOfWork APPLICATION_NAME, BOUNDED_CONTEXT_NAME, NO_CORRELATION_ID, USER_UNKNOWN, publish(expectedEventEnvelope)
//        }
//
//        verify(mockEventConsumer, never()).process(expectedEventEnvelope)
    }

}
