package de.oneos.cqrs.readmodels.amqp

import de.oneos.eventsourcing.Event
import de.oneos.eventsourcing.EventEnvelope
import org.junit.After
import org.junit.Before
import org.junit.Test
import com.rabbitmq.client.*

import static java.util.UUID.randomUUID
import static org.hamcrest.CoreMatchers.equalTo
import static org.junit.Assert.assertThat

import de.oneos.eventsourcing.EventPublishingException

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
        connection = factory.newConnection();

        eventPublisher = new AMQPEventPublisher(connection)

        consumerChannel = connection.createChannel()
        def declareOk = consumerChannel.queueDeclare()
        consumerChannel.queueBind(declareOk.queue, AMQPConstants.EVENT_EXCHANGE_NAME, "*.*.*.${anEvent.name}")

        consumer = new QueueingConsumer(consumerChannel);
        consumerChannel.basicConsume(declareOk.queue, de.oneos.cqrs.readmodels.amqp.AMQPConstants.AUTO_ACK, consumer);
    }


    @Test
    void should_send_a_serialized_event_to_the_message_broker() {
        def eventEnvelope = new EventEnvelope('CQRS Core Library', 'Tests', 'Aggregate', aggregateId, anEvent)
        eventPublisher.publish(eventEnvelope)

        assertThat receivedMessage(), equalTo(eventEnvelope.toJSON())
    }

    @Test(expected = EventPublishingException)
    void should_throw_Exception_when_event_coordinate_contains_a_dot() {
        def eventEnvelope = new EventEnvelope('CQRS.Core Library', 'CQRS.Tests', 'CQRS.Aggregate', aggregateId, anEvent)
        eventPublisher.publish(eventEnvelope)
    }

    private String receivedMessage() {
        QueueingConsumer.Delivery delivery = consumer.nextDelivery();
        return new String(delivery.getBody());
    }

    @After
    void tearDown() throws Exception {
        consumerChannel.close()
        connection.close()
    }


    static class Business_event_happened extends Event {
        @Override
        def applyTo(aggregate) { aggregate }
    }
}
