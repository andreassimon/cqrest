package de.oneos.eventselection.amqp

import java.util.concurrent.CountDownLatch

import org.junit.*
import static org.junit.Assert.*
import static org.hamcrest.Matchers.*
import static org.mockito.Mockito.*

import com.rabbitmq.client.*

import de.oneos.eventstore.*


class AMQPEventTransportTest {
    static final String APPLICATION = 'APPLICATION'
    static final String BOUNDED_CONTEXT = 'BOUNDED CONTEXT'
    static final String AGGREGATE = 'AGGREGATE'
    static final String USER = 'USER'

    static final UUID AGGREGATE_ID = UUID.fromString('8a4e8dec-1565-43ca-88d5-fd31541d7041')
    static final UUID CORRELATION_ID = UUID.fromString('d158a594-c864-4f36-ab34-238de28e8015')

    final thiz = this

    StubEventSupplier eventStore
    EventProcessor eventPublisher
    EventProcessor eventProcessor
    AMQPEventSupplier eventSupplier

    EventEnvelope publishedEventEnvelope

    final String theEventName = 'My event'

    final List queryResults = [
        new EventEnvelope(APPLICATION, BOUNDED_CONTEXT, AGGREGATE, AGGREGATE_ID, [ eventName: theEventName, eventAttributes: [attribute:   'one'] ], 0, new Date(2013 - 1900, 5, 11, 12, 00), CORRELATION_ID, USER),
        new EventEnvelope(APPLICATION, BOUNDED_CONTEXT, AGGREGATE, AGGREGATE_ID, [ eventName: theEventName, eventAttributes: [attribute:   'two'] ], 1, new Date(2013 - 1900, 5, 11, 12, 01), CORRELATION_ID, USER),
        new EventEnvelope(APPLICATION, BOUNDED_CONTEXT, AGGREGATE, AGGREGATE_ID, [ eventName: theEventName, eventAttributes: [attribute: 'three'] ], 2, new Date(2013 - 1900, 5, 11, 12, 02), CORRELATION_ID, USER),
    ]

    CountDownLatch lock


    @Before
    void setUp() {
        ConnectionFactory connectionFactory = new ConnectionFactory();
        connectionFactory.virtualHost = 'one-os-test'
        Connection connection = connectionFactory.newConnection()

        eventPublisher = new AMQPEventPublisher(connection)

        eventProcessor = mock(EventProcessor)
        eventSupplier = new AMQPEventSupplier(connection)

        publishedEventEnvelope = new EventEnvelope(APPLICATION, BOUNDED_CONTEXT, AGGREGATE, AGGREGATE_ID, [ eventName: 'My event', eventAttributes: [attribute: 'value'] ], 0, new Date(2013 - 1900, 5, 11, 12, 00), CORRELATION_ID, USER)
    }

    @Test
    void should_notify_registration_to_EventProcessor() {
        eventSupplier.eventProcessors = [eventProcessor]

        verify(eventProcessor).wasRegisteredAt(eventSupplier)
    }

    @Test(timeout = 2000L)
    void should_deliver_published_EventEnvelopes_to_registered_EventProcessors() {
        eventSupplier.eventProcessors = [synchronize(eventProcessor)]

        publish(publishedEventEnvelope)

        verify(eventProcessor).process(publishedEventEnvelope)
    }

    @Test(timeout = 2000L)
    void withEventEnvelopes__should_call_block_with_query_results() {
        lock = new CountDownLatch(queryResults.size())
        eventStore = new StubEventSupplier(queryResult: queryResults)
        eventPublisher.wasRegisteredAt(eventStore)

        def actual = []
        eventSupplier.withEventEnvelopes([eventName: theEventName]) { EventEnvelope eventEnvelope ->
            lock.countDown()
            actual << eventEnvelope
        }

        lock.await()
        assertThat actual, equalTo(queryResults)
    }

    protected EventProcessor synchronize(EventProcessor targetEventProcessor) {
        return [
            wasRegisteredAt: {},
            process: { EventEnvelope eventEnvelope ->
                targetEventProcessor.process(eventEnvelope)
                synchronized(thiz) {
                    thiz.notifyAll();
                }
            }
        ] as EventProcessor
    }

    protected void publish(EventEnvelope eventEnvelope) {
        eventPublisher.process(eventEnvelope)
        synchronized(thiz) {
            thiz.wait(3000);
        }
    }

}

class StubEventSupplier implements EventSupplier {

    List<EventEnvelope> queryResult

    @Override
    void subscribeTo(Map<String, ?> criteria, EventProcessor eventProcessor) {
        throw new RuntimeException('StubEventSupplier.subscribeTo() is not implemented')
    }

    @Override
    void withEventEnvelopes(Map<String, ?> criteria, Closure block) {
        queryResult.each(block)
    }

}
