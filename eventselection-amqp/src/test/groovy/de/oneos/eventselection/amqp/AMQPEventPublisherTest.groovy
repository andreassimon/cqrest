package de.oneos.eventselection.amqp

import org.junit.*
import static org.hamcrest.CoreMatchers.*
import static org.junit.Assert.*

import static java.util.UUID.*

import com.rabbitmq.client.*

import de.oneos.eventsourcing.BaseEvent
import de.oneos.eventsourcing.Event
import de.oneos.eventsourcing.EventEnvelope
import de.oneos.eventsourcing.EventProcessingException
import de.oneos.eventsourcing.EventSupplier


class AMQPEventPublisherTest {
    static final UUID NO_CORRELATION_ID = null
    static final String USER_UNKNOWN = null

    EventSupplier upstreamSupplier = new StubEventSupplier()
    AMQPEventPublisher eventPublisher
    Connection connection
    Consumer consumer
    Channel consumerChannel

    UUID aggregateId = randomUUID()
    final Event anEvent = new Business_event_happened()

    @Before
    void setUp() throws Exception {
        ConnectionFactory factory = new ConnectionFactory();
        factory.virtualHost = 'cqrs-test'
        try {
            connection = factory.newConnection();
        } catch (ConnectException) {
            fail('Couldn\'t connect to AMQP. Try running `bin/services start`.')
        }

        eventPublisher = new AMQPEventPublisher(connection, upstreamSupplier)

        consumerChannel = connection.createChannel()
        def declareOk = consumerChannel.queueDeclare()
        consumerChannel.queueBind(declareOk.queue, AMQP.EVENT_EXCHANGE_NAME, "*.*.*.${anEvent.eventName}")

        consumer = new QueueingConsumer(consumerChannel);
        consumerChannel.basicConsume(declareOk.queue, AMQP.AUTO_ACK, consumer);
    }


    @Test
    void should_send_a_serialized_event_to_the_message_broker() {
        def eventEnvelope = new EventEnvelope('Readmodels Library', 'AMQP Tests', 'An Aggregate', aggregateId, anEvent, NO_CORRELATION_ID, USER_UNKNOWN)
        eventPublisher.process(eventEnvelope)

        assertThat receivedMessage(), equalTo(eventEnvelope.toJSON())
    }

    @Test(expected = EventProcessingException)
    void should_throw_Exception_when_event_coordinate_contains_a_dot() {
        def eventEnvelope = new EventEnvelope('Readmodels.Library', 'AMQP.Tests', 'An.Aggregate', aggregateId, anEvent, NO_CORRELATION_ID, USER_UNKNOWN)
        eventPublisher.process(eventEnvelope)
    }

    private String receivedMessage() {
        QueueingConsumer.Delivery delivery = consumer.nextDelivery(10);
        if(!delivery) {
            fail('No message could be received')
        }
        return new String(delivery.getBody());
    }

    @After
    void tearDown() throws Exception {
        if (consumerChannel) { consumerChannel.close() }
        if (connection) { connection.close() }
    }


    static class Business_event_happened extends BaseEvent { }
}
