package infrastructure.messaging

import com.rabbitmq.client.Channel
import com.rabbitmq.client.ConnectionFactory
import com.rabbitmq.client.QueueingConsumer
import com.rabbitmq.client.impl.AMQConnection
import domain.aggregates.Device
import domain.events.Event
import domain.events.New_device_was_registered
import org.junit.Test

import static infrastructure.utilities.GenericEventSerializer.toJSON
import static org.hamcrest.CoreMatchers.equalTo
import static org.hamcrest.CoreMatchers.is
import static org.junit.Assert.assertThat

class AMQPEventPublisherTest {

    def eventPublisher = new AMQPEventPublisher()


    @Test
    public void should_send_a_serialized_event_to_the_message_broker() {
        final Event<Device> event = new New_device_was_registered(new Device.Id(), "new device name")

        eventPublisher.publish(event)

        assertThat receivedMessage(), is(equalTo(toJSON(event)))
    }

    private String receivedMessage() {
        ConnectionFactory factory = new ConnectionFactory();
        factory.setHost("localhost");
        AMQConnection connection = factory.newConnection();
        Channel channel = connection.createChannel();

        channel.queueDeclare(AMQPConstants.EVENT_QUEUE, AMQPConstants.NOT_DURABLE, AMQPConstants.NOT_EXCLUSIVE, AMQPConstants.NO_AUTO_DELETE, AMQPConstants.NO_ADDITIONAL_ARGUMENTS);

        QueueingConsumer consumer = new QueueingConsumer(channel);
        channel.basicConsume(AMQPConstants.EVENT_QUEUE, AMQPConstants.AUTO_ACK, consumer);

        QueueingConsumer.Delivery delivery = consumer.nextDelivery();
        return new String(delivery.getBody());
    }
}
