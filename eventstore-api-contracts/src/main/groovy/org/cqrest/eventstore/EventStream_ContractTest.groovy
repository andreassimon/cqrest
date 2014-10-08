package org.cqrest.eventstore

import org.junit.Test

import org.cqrest.eventsourcing.EventEnvelope
import org.cqrest.eventsourcing.EventStream

import org.cqrest.reactive.test.MockObserver
import static org.cqrest.test.AnEventEnvelope.anEventEnvelope


abstract class EventStream_ContractTest {
    public static UUID ORDER_ID = UUID.fromString('836ed0d1-e87f-4d70-80f3-7aa44d00ed5d')
    public static UUID ANOTHER_ORDER_ID = UUID.fromString('92bc8efe-0f5e-42f7-8dd6-3029d2d1a4eb')


    abstract EventStream getEventStream()

    abstract void setStreamHistory(List<EventEnvelope> history)


    @Test
    void observe__should_send_EventEnvelopes_to_subscribed_Observers() {
        final List<EventEnvelope> history = [
          anEventEnvelope().withAggregateId(ORDER_ID).withEventName('Order line was added').withEventAttributes(article: null).build(),
          anEventEnvelope().withAggregateId(ANOTHER_ORDER_ID).withEventName('Order line was added').withEventAttributes(article: null).build(),
        ]
        setStreamHistory(history)
        final MockObserver observer = new MockObserver(history)

        eventStream.observe().subscribe(observer)

        observer.assertReceivedEvents()
    }


    // TODO pull up `observe__should_not_complete_streams_of_Observers` here

    @Test
    void observe__should_filter_EventEnvelopes_by_event_names() {
        final notMatching = [ anEventEnvelope().withAggregateId(ORDER_ID).withEventName('Order line added').build() ]
        final matching =    [ anEventEnvelope().withAggregateId(ANOTHER_ORDER_ID).withEventName('Order line removed').build() ]
        setStreamHistory(notMatching + matching)
        final MockObserver observer = new MockObserver(matching)

        eventStream.observe(eventName: 'Order line removed').subscribe(observer)

        observer.assertReceivedEvents()
    }

    @Test
    void should_send_any_exceptions_thrown_while_replaying_to_onError() {
        final history = [ anEventEnvelope().withEventName('Order line added').build() ]
        setStreamHistory(history)
        def defectiveObserver = new DefectiveObserver()
        def flawlessObserver = new MockObserver(history)

        [defectiveObserver, flawlessObserver].each {
            eventStream.observe().subscribe(it)
        }

        defectiveObserver.assertReceivedExceptionIn_onError()
        flawlessObserver.assertReceivedEvents()
    }

    @Test
    void should_pass_new_persisted_events_to_subscribed_Observers() {
        final notMatching = [ anEventEnvelope().withSequenceNumber(0).withEventName('Order line added').build() ]
        final matching =    [ anEventEnvelope().withSequenceNumber(1).withEventName('Order line removed').build() ]

        final MockObserver observer = new MockObserver(matching)
        eventStream.observe(eventName: 'Order line removed').subscribe(observer)

        setStreamHistory(notMatching + matching)

        observer.assertReceivedEvents()
    }


    // TODO pull up should_not_pass_new_persisted_events_to_unsubscribed_Observers() here

    // TODO pull up should_send_any_exceptions_thrown_by_onNext_to_onError() here

    // TODO pull up should_log_any_exceptions_thrown_by_onError() here

    // TODO pull up should_not_publish_any_event_when_transaction_failed() here

}
