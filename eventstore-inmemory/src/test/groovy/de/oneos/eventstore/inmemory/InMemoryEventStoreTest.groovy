package de.oneos.eventstore.inmemory

import org.junit.*

import static org.mockito.Mockito.mock
import static org.mockito.Mockito.reset
import static org.mockito.Mockito.verify
import static org.mockito.Mockito.verifyNoMoreInteractions

import org.cqrest.reactive.test.MockObserver
import static de.oneos.eventstore.inmemory.AnEventEnvelope.anEventEnvelope

import rx.plugins.RxJavaErrorHandler
import rx.plugins.RxJavaPlugins

import de.oneos.eventsourcing.EventEnvelope
import de.oneos.eventstore.EventCollisionOccurred
import de.oneos.eventstore.EventStore_ContractTest


class InMemoryEventStoreTest extends EventStore_ContractTest {

    private static RxJavaErrorHandler mockErrorHandler

    InMemoryEventStore eventStore

    @BeforeClass
    static void setUpRxJavaErrorHandler() {
        mockErrorHandler = mock(RxJavaErrorHandler)
        // Might lead to interacting tests
        RxJavaPlugins.getInstance().registerErrorHandler(mockErrorHandler)
    }

    @Override
    @Before
    public void setUp() {
        super.setUp()

        eventStore = new InMemoryEventStore()
        reset(mockErrorHandler)
    }

    @Test
    void observe__should_send_EventEnvelopes_to_subscribed_Observers() {
        eventStore.history = [
          anEventEnvelope().withAggregateId(ORDER_ID).withEventName('Order line was added').withEventAttributes(article: null).build(),
          anEventEnvelope().withAggregateId(ANOTHER_ORDER_ID).withEventName('Order line was added').withEventAttributes(article: null).build(),
        ]
        final MockObserver observer = new MockObserver(eventStore.history)

        eventStore.observe().subscribe(observer)

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

    @Test
    void should_send_any_exceptions_thrown_by_onNext_to_onError() {
        final newEvents = [ anEventEnvelope().withEventName('Order line added').build() ]
        def defectiveObserver = new DefectiveObserver()
        def flawlessObserver = new MockObserver(newEvents)
        [defectiveObserver, flawlessObserver].each {
            eventStore.observe().subscribe(it)
        }

        eventStore.saveEnvelopes(newEvents)

        defectiveObserver.assertReceivedExceptionIn_onError()
        flawlessObserver.assertReceivedEvents()
    }

    @Test
    void should_log_any_exceptions_thrown_by_onError() {
        final newEvents = [ anEventEnvelope().build() ]
        def totallyDefectiveObserver = new TotallyDefectiveObserver()
        eventStore.observe().subscribe(totallyDefectiveObserver)

        eventStore.saveEnvelopes(newEvents)

        verify(mockErrorHandler).handleError(TotallyDefectiveObserver.THROWN_BY_ON_NEXT)
        verify(mockErrorHandler).handleError(TotallyDefectiveObserver.THROWN_BY_ON_ERROR)
        verifyNoMoreInteractions(mockErrorHandler)
    }

    @Test
    void should_not_publish_any_event_when_transaction_failed() {
        org.cqrest.reactive.Observer<EventEnvelope> mockObserver = mock(org.cqrest.reactive.Observer)
        eventStore.observe().subscribe(mockObserver)
        eventStore.saveEnvelopes([ anEventEnvelope().withSequenceNumber(0).build() ])
        reset(mockObserver)

        expect(EventCollisionOccurred) {
            eventStore.saveEnvelopes([ anEventEnvelope().withSequenceNumber(0).build() ])
        }

        verifyNoMoreInteractions(mockObserver)
    }

}
