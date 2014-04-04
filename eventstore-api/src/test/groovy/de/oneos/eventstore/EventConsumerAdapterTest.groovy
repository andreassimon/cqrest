package de.oneos.eventstore;

import org.junit.Test

import static org.mockito.Mockito.mock
import static org.mockito.Mockito.verify

import static org.cqrest.test.AnEventEnvelope.anEventEnvelope;

import de.oneos.eventsourcing.EventConsumer;
import de.oneos.eventsourcing.EventEnvelope


public class EventConsumerAdapterTest {

    @Test
    public void onNext_should_pass_the_EventEnvelope_to_the_wrapped_EventConsumer() throws Exception {
        EventConsumer mockEventConsumer = mock(EventConsumer)

        rx.Observer<EventEnvelope> adapter = new EventConsumerAdapter(mockEventConsumer)
        adapter.onNext(anEventEnvelope().build())

        verify(mockEventConsumer).process(anEventEnvelope().build())
    }

}

