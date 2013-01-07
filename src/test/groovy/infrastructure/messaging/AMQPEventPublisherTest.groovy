package infrastructure.messaging

import com.rabbitmq.client.*
import com.rabbitmq.client.impl.AMQConnection
import domain.aggregates.Device
import domain.events.*
import org.junit.*
import readmodels.ReadModelBuilder

import static infrastructure.messaging.AMQPConstants.*
import static infrastructure.utilities.GenericEventSerializer.toJSON
import static java.util.UUID.randomUUID
import static org.hamcrest.CoreMatchers.*
import static org.junit.Assert.assertThat

class AMQPEventPublisherTest {

    def eventPublisher = new AMQPEventPublisher()


    @Test
    public void should_send_a_serialized_event_to_the_message_broker() {
        final Event<Device> event = new New_device_was_registered(randomUUID(), "new device name")

        eventPublisher.publish(event)

        assertThat receivedMessage(), is(equalTo(toJSON(event)))
    }

    private String receivedMessage() {
        ConnectionFactory factory = new ConnectionFactory();
        factory.setHost("localhost");
        AMQConnection connection = factory.newConnection();
        Channel channel = connection.createChannel();

        channel.queueDeclare(ReadModelBuilder.MESSAGE_QUEUE, NOT_DURABLE, NOT_EXCLUSIVE, NO_AUTO_DELETE, NO_ADDITIONAL_ARGUMENTS);

        QueueingConsumer consumer = new QueueingConsumer(channel);
        channel.basicConsume(ReadModelBuilder.MESSAGE_QUEUE, AUTO_ACK, consumer);

        QueueingConsumer.Delivery delivery = consumer.nextDelivery();
        return new String(delivery.getBody());
    }
}
