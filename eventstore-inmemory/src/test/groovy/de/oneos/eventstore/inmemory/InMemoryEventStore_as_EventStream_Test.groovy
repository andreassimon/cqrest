package de.oneos.eventstore.inmemory

import org.junit.Before
import org.junit.BeforeClass
import org.junit.Test
import static org.mockito.Mockito.mock
import static org.mockito.Mockito.never
import static org.mockito.Mockito.reset
import static org.mockito.Mockito.verify
import static org.mockito.Mockito.verifyNoMoreInteractions

import rx.plugins.RxJavaErrorHandler
import rx.plugins.RxJavaPlugins

import de.oneos.eventsourcing.EventEnvelope
import de.oneos.eventsourcing.EventStream
import de.oneos.eventstore.EventCollisionOccurred

import org.cqrest.eventstore.DefectiveObserver
import static org.cqrest.test.AnEventEnvelope.anEventEnvelope

import org.cqrest.eventstore.EventStream_ContractTest
import org.cqrest.reactive.test.MockObserver
import static org.cqrest.test.Expect.expect


class InMemoryEventStore_as_EventStream_Test extends EventStream_ContractTest {
    private static RxJavaErrorHandler mockErrorHandler

    InMemoryEventStore eventStore

    @Override
    EventStream getEventStream() {
        return eventStore
    }

    @Override
    void setStreamHistory(List<EventEnvelope> history) {
        eventStore.saveEnvelopes(history)
    }

    @BeforeClass
    static void setUpRxJavaErrorHandler() {
        mockErrorHandler = mock(RxJavaErrorHandler)
        // Might lead to interacting tests
        RxJavaPlugins.getInstance().registerErrorHandler(mockErrorHandler)
    }

    @Before
    public void setUp() {
        eventStore = new InMemoryEventStore()
        reset(mockErrorHandler)
    }

    @Test
    void observe__should_not_complete_streams_of_Observers() {
        eventStore.history = [
          anEventEnvelope().withAggregateId(ORDER_ID).withEventName('Order line was added').withEventAttributes(article: null).build(),
          anEventEnvelope().withAggregateId(ANOTHER_ORDER_ID).withEventName('Order line was added').withEventAttributes(article: null).build(),
        ]
        final org.cqrest.reactive.Observer<EventEnvelope> observer = mock(org.cqrest.reactive.Observer)

        eventStore.observe().subscribe(observer)

        verify(observer, never()).onCompleted()
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

    @Test
    void should_not_pass_new_persisted_events_to_unsubscribed_Observers() {
        final org.cqrest.reactive.Observer<EventEnvelope> observer = mock(org.cqrest.reactive.Observer)
        def subscription = eventStore.observe().subscribe(observer)

        subscription.unsubscribe()
        eventStore.saveEnvelopes([ anEventEnvelope().withEventName('Order line removed').build() ])

        verifyNoMoreInteractions(observer)
    }

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
