package infrastructure.messaging

import com.rabbitmq.client.Channel
import com.rabbitmq.client.ConnectionFactory
import com.rabbitmq.client.QueueingConsumer
import com.rabbitmq.client.impl.AMQConnection
import domain.aggregates.Device
import domain.events.Event
import domain.events.New_device_was_registered
import infrastructure.utilities.GenericEventSerializer
import org.junit.Test

import static infrastructure.utilities.GenericEventSerializer.toJSON
import static org.hamcrest.CoreMatchers.equalTo
import static org.junit.Assert.assertThat

class AMQPEventPublisherTest {

    public static final boolean AUTO_ACK = true

    def eventPublisher = new AMQPEventPublisher()


    @Test
    public void should_send_a_serialized_event_to_the_message_broker() {
        final Event<Device> event = new New_device_was_registered(new Device.Id(), "new device name")

        eventPublisher.publish(event)

        assertThat(receivedMessage(), equalTo(toJSON(event)))
    }

    private String receivedMessage() {
        ConnectionFactory factory = new ConnectionFactory();
        factory.setHost("localhost");
        AMQConnection connection = factory.newConnection();
        Channel channel = connection.createChannel();

        channel.queueDeclare(AMQPEventPublisher.QUEUE_NAME, AMQPEventPublisher.NOT_DURABLE, AMQPEventPublisher.NOT_EXCLUSIVE, AMQPEventPublisher.NO_AUTO_DELETE, AMQPEventPublisher.NO_ADDITIONAL_ARGUMENTS);

        QueueingConsumer consumer = new QueueingConsumer(channel);
        channel.basicConsume(AMQPEventPublisher.QUEUE_NAME, AUTO_ACK, consumer);

        QueueingConsumer.Delivery delivery = consumer.nextDelivery();
        return new String(delivery.getBody());
    }
}
