package de.oneos.eventselection.amqp

import java.util.concurrent.CountDownLatch

import org.junit.*

import com.rabbitmq.client.*

import de.oneos.eventsourcing.EventEnvelope


class AMQPEventTransportTest {
    static final String APPLICATION = 'APPLICATION'
    static final String BOUNDED_CONTEXT = 'BOUNDED CONTEXT'
    static final String AGGREGATE = 'AGGREGATE'
    static final String USER = 'USER'

    static final UUID AGGREGATE_ID = UUID.fromString('8a4e8dec-1565-43ca-88d5-fd31541d7041')
    static final UUID CORRELATION_ID = UUID.fromString('d158a594-c864-4f36-ab34-238de28e8015')

    static StubEventSupplier eventStore
    static AMQPEventPublisher eventPublisher
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
        connectionFactory.virtualHost = 'one-os-test'
        Connection connection = connectionFactory.newConnection()

        eventStore = new StubEventSupplier(queryResult: queryResults)

        eventPublisher = new AMQPEventPublisher(connection, eventStore)

        eventSupplier = new AMQPEventSupplier(connection, eventPublisher)

        publishedEventEnvelope = new EventEnvelope(APPLICATION, BOUNDED_CONTEXT, AGGREGATE, AGGREGATE_ID, [ eventName: 'My event', eventAttributes: [attribute: 'value'] ], 0, new Date(2013 - 1900, 5, 11, 12, 00), CORRELATION_ID, USER)
    }

    @Test(timeout = 2000L)
    void should_deliver_published_EventEnvelopes_to_registered_EventConsumer() {
        def invocation = new CountDownLatch(1)
        eventSupplier.observe([:]).subscribe([
            onNext: { invocation.countDown() }
        ] as org.cqrest.reactive.Observer<EventEnvelope>)

        eventPublisher.onNext(publishedEventEnvelope)

        invocation.await()
    }

    @Test(timeout = 2000L)
    void should_pass_matching_past_events_to_the_subscribed_observers() {
        lock = new CountDownLatch(queryResults.size())

        Iterator<EventEnvelope> expected = queryResults.iterator()
        eventSupplier.observe(eventName: theEventName).subscribe([
          onNext: { EventEnvelope eventEnvelope ->
              assert expected.next() == eventEnvelope
              lock.countDown()
          }
        ] as org.cqrest.reactive.Observer<EventEnvelope>)

        lock.await()
    }

}
