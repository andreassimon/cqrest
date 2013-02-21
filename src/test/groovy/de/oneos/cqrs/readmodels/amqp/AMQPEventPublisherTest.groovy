package de.oneos.cqrs.readmodels.amqp

import domain.events.Event
import domain.events.EventEnvelope
import oneos.test.domain.aggregates.Device
import oneos.test.domain.events.Device_was_registered
import org.junit.After
import org.junit.Before
import org.junit.Test
import com.rabbitmq.client.*

import static java.util.UUID.randomUUID
import static org.hamcrest.CoreMatchers.equalTo
import static org.junit.Assert.assertThat
import framework.PublishingException

class AMQPEventPublisherTest {

    AMQPEventPublisher eventPublisher
    Connection connection
    Consumer consumer
    Channel consumerChannel

    UUID deviceId = randomUUID()
    final Event<Device> anEvent = new Device_was_registered(deviceId: deviceId, deviceName: "new device name")

    @Before
    void setUp() throws Exception {
        ConnectionFactory factory = new ConnectionFactory();
        factory.virtualHost = 'one-os-test'
        connection = factory.newConnection();

        eventPublisher = new AMQPEventPublisher(connection)

        consumerChannel = connection.createChannel()
        def declareOk = consumerChannel.queueDeclare()
        consumerChannel.queueBind(declareOk.queue, AMQPConstants.EVENT_EXCHANGE_NAME, '*.*.*.Device was registered')

        consumer = new QueueingConsumer(consumerChannel);
        consumerChannel.basicConsume(declareOk.queue, de.oneos.cqrs.readmodels.amqp.AMQPConstants.AUTO_ACK, consumer);
    }


    @Test
    void should_send_a_serialized_event_to_the_message_broker() {
        def eventEnvelope = new EventEnvelope<Device>('CQRS Core Library', 'Tests', 'Device', deviceId, anEvent)
        eventPublisher.publish(eventEnvelope)

        assertThat receivedMessage(), equalTo(eventEnvelope.toJSON())
    }

    @Test(expected = PublishingException)
    void should_throw_Exception_when_event_coordinate_contains_a_dot() {
        def eventEnvelope = new EventEnvelope<Device>('CQRS.Core Library', 'CQRS.Tests', 'CQRS.Device', deviceId, anEvent)
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
}
