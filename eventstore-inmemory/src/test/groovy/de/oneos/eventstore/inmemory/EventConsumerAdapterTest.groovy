package de.oneos.eventstore.inmemory;

import org.junit.Test

import static org.mockito.Mockito.mock
import static org.mockito.Mockito.verify;

import static de.oneos.eventstore.inmemory.AnEventEnvelope.anEventEnvelope;

import de.oneos.eventsourcing.EventConsumer;
import de.oneos.eventsourcing.EventEnvelope
import org.cqrest.reactive.Observer;


public class EventConsumerAdapterTest {

    @Test
    public void onNext_should_pass_the_EventEnvelope_to_the_wrapped_EventConsumer() throws Exception {
        EventConsumer mockEventConsumer = mock(EventConsumer)

        Observer<EventEnvelope> adapter = new EventConsumerAdapter(mockEventConsumer)
        adapter.onNext(anEventEnvelope().build())

        verify(mockEventConsumer).process(anEventEnvelope().build())
    }

}

