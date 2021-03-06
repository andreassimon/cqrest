package org.cqrest.eventbus.amqp

import java.util.concurrent.CountDownLatch

import org.junit.*
import static org.junit.Assert.*
import static org.hamcrest.Matchers.*
import static org.mockito.Mockito.*

import com.rabbitmq.client.*

import org.cqrest.eventsourcing.EventConsumer
import org.cqrest.eventsourcing.EventEnvelope


class AMQPEventTransportTest {
    static final String APPLICATION = 'APPLICATION'
    static final String BOUNDED_CONTEXT = 'BOUNDED CONTEXT'
    static final String AGGREGATE = 'AGGREGATE'
    static final String USER = 'USER'

    static final UUID AGGREGATE_ID = UUID.fromString('8a4e8dec-1565-43ca-88d5-fd31541d7041')
    static final UUID CORRELATION_ID = UUID.fromString('d158a594-c864-4f36-ab34-238de28e8015')

    static StubEventSupplier eventStore
    static EventConsumer eventPublisher
    static AMQPEventSupplier eventSupplier

    static EventEnvelope publishedEventEnvelope

    static final String theEventName = 'My event'

    static final List queryResults = [
        new EventEnvelope(APPLICATION, BOUNDED_CONTEXT, AGGREGATE, AGGREGATE_ID, [ eventName: theEventName, eventAttributes: [attribute:   'one'] ], 0, new Date(2013 - 1900, 5, 11, 12, 00), CORRELATION_ID, USER),
        new EventEnvelope(APPLICATION, BOUNDED_CONTEXT, AGGREGATE, AGGREGATE_ID, [ eventName: theEventName, eventAttributes: [attribute:   'two'] ], 1, new Date(2013 - 1900, 5, 11, 12, 01), CORRELATION_ID, USER),
        new EventEnvelope(APPLICATION, BOUNDED_CONTEXT, AGGREGATE, AGGREGATE_ID, [ eventName: theEventName, eventAttributes: [attribute: 'three'] ], 2, new Date(2013 - 1900, 5, 11, 12, 02), CORRELATION_ID, USER),
    ]

    CountDownLatch lock


    @BeforeClass
    static void setUp() {
        ConnectionFactory connectionFactory = new ConnectionFactory();
        connectionFactory.virtualHost = 'cqrs-test'
        Connection connection = connectionFactory.newConnection()

        eventStore = new StubEventSupplier(queryResult: queryResults)

        eventPublisher = new AMQPEventPublisher(connection, eventStore)

        eventSupplier = new AMQPEventSupplier(connection, eventPublisher)

        publishedEventEnvelope = new EventEnvelope(APPLICATION, BOUNDED_CONTEXT, AGGREGATE, AGGREGATE_ID, [ eventName: 'My event', eventAttributes: [attribute: 'value'] ], 0, new Date(2013 - 1900, 5, 11, 12, 00), CORRELATION_ID, USER)
    }

    @Test(timeout = 2000L)
    void should_deliver_published_EventEnvelopes_to_registered_EventConsumer() {
        def invocation = new CountDownLatch(1)
        eventSupplier.subscribeTo([:], [
            process: { invocation.countDown() }
        ] as EventConsumer)

        publish(publishedEventEnvelope)

        invocation.await()
    }

    @Test(timeout = 2000L)
    void withEventEnvelopes__should_call_block_with_query_results() {
        lock = new CountDownLatch(queryResults.size())

        def actual = Collections.synchronizedList([])
        eventSupplier.withEventEnvelopes([eventName: theEventName]) { EventEnvelope eventEnvelope ->
            actual << eventEnvelope
            lock.countDown()
        }

        lock.await()
        synchronized(actual) {
            assertThat actual, equalTo(queryResults)
        }
    }

    protected void publish(EventEnvelope eventEnvelope) {
        eventPublisher.process(eventEnvelope)
    }

}
