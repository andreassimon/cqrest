package org.cqrest.eventstore

import org.junit.Test

import de.oneos.eventsourcing.EventEnvelope
import de.oneos.eventsourcing.EventStream

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


    // TODO insert here `observe__should_not_complete_streams_of_Observers`

    @Test
    void observe__should_filter_EventEnvelopes_by_event_names() {
        final notMatching = [ anEventEnvelope().withAggregateId(ORDER_ID).withEventName('Order line added').build() ]
        final matching =    [ anEventEnvelope().withAggregateId(ANOTHER_ORDER_ID).withEventName('Order line removed').build() ]
        setStreamHistory(notMatching + matching)
        final MockObserver observer = new MockObserver(matching)

        eventStream.observe(eventName: 'Order line removed').subscribe(observer)

        observer.assertReceivedEvents()
    }

}
