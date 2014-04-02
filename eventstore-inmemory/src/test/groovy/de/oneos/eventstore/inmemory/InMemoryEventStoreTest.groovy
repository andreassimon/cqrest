package de.oneos.eventstore.inmemory

import org.junit.*

import org.cqrest.reactive.test.MockObserver
import static de.oneos.eventstore.inmemory.AnEventEnvelope.anEventEnvelope

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
          anEventEnvelope().withAggregateId(ORDER_ID).withEventName('Order line was added').withEventAttributes(article: null).build(),
          anEventEnvelope().withAggregateId(ANOTHER_ORDER_ID).withEventName('Order line was added').withEventAttributes(article: null).build(),
        ]
        final MockObserver observer = new MockObserver(eventStore.history)

        eventStore.observe([:]).subscribe(observer)

        observer.assertReceivedEvents()
    }

    @Test
    void observe__should_filter_EventEnvelopes_by_event_names() {
        final notMatching = [ anEventEnvelope().withAggregateId(ORDER_ID).withEventName('Order line added').build() ]
        final matching =    [ anEventEnvelope().withAggregateId(ANOTHER_ORDER_ID).withEventName('Order line removed').build() ]
        eventStore.history = notMatching + matching
        final MockObserver observer = new MockObserver(matching)

        eventStore.observe(eventName: 'Order line removed').subscribe(observer)

        observer.assertReceivedEvents()
    }


    @Test
    void should_pass_new_persisted_events_to_subscribed_Observers() {
        final notMatching = [ anEventEnvelope().withEventName('Order line added').build() ]
        final matching =    [ anEventEnvelope().withEventName('Order line removed').build() ]

        final MockObserver observer = new MockObserver(matching)
        eventStore.observe(eventName: 'Order line removed').subscribe(observer)

        eventStore.saveEnvelopes(notMatching + matching)

        observer.assertReceivedEvents()
    }

    // TODO test closing subscriptions

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
