package de.oneos.eventselection.amqp

import com.rabbitmq.client.*
import de.oneos.eventsourcing.*
import de.oneos.eventstore.*
import org.junit.*

import static java.util.UUID.*
import static org.hamcrest.CoreMatchers.*
import static org.junit.Assert.*

class AMQPEventPublisherTest {

    AMQPEventPublisher eventPublisher
    Connection connection
    Consumer consumer
    Channel consumerChannel

    UUID aggregateId = randomUUID()
    final Event anEvent = new Business_event_happened()

    @Before
    void setUp() throws Exception {
        ConnectionFactory factory = new ConnectionFactory();
        factory.virtualHost = 'one-os-test'
        try {
            connection = factory.newConnection();
        } catch (ConnectException) {
            fail('Couldn\'t connect to AMQP. Try running `bin/services start`.')
        }

        eventPublisher = new AMQPEventPublisher(connection)

        consumerChannel = connection.createChannel()
        def declareOk = consumerChannel.queueDeclare()
        consumerChannel.queueBind(declareOk.queue, AMQPConstants.EVENT_EXCHANGE_NAME, "*.*.*.${anEvent.eventName}")

        consumer = new QueueingConsumer(consumerChannel);
        consumerChannel.basicConsume(declareOk.queue, de.oneos.eventselection.amqp.AMQPConstants.AUTO_ACK, consumer);
    }


    @Test
    void should_send_a_serialized_event_to_the_message_broker() {
        def eventEnvelope = new EventEnvelope('Readmodels Library', 'AMQP Tests', 'An Aggregate', aggregateId, anEvent)
        eventPublisher.publish(eventEnvelope)

        assertThat receivedMessage(), equalTo(eventEnvelope.toJSON())
    }

    @Test(expected = EventPublishingException)
    void should_throw_Exception_when_event_coordinate_contains_a_dot() {
        def eventEnvelope = new EventEnvelope('Readmodels.Library', 'AMQP.Tests', 'An.Aggregate', aggregateId, anEvent)
        eventPublisher.publish(eventEnvelope)
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


    static class Business_event_happened extends Event {
        @Override
        void applyTo(aggregate) { aggregate }
    }
}
