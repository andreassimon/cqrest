package infrastructure.messaging

import com.rabbitmq.client.*

import domain.events.*
import org.junit.*

import static infrastructure.messaging.AMQPConstants.*
import static infrastructure.utilities.GenericEventSerializer.toJSON
import static java.util.UUID.randomUUID
import static org.hamcrest.CoreMatchers.*
import static org.junit.Assert.assertThat
import domain.aggregates.Device

class AMQPEventPublisherTest {

    def eventPublisher
    Connection connection
    Consumer consumer
    Channel consumerChannel

    @Before
    public void setUp() throws Exception {
        ConnectionFactory factory = new ConnectionFactory();
        factory.virtualHost = 'one-os-test'
        connection = factory.newConnection();

        eventPublisher = new AMQPEventPublisher(connection)

        consumerChannel = connection.createChannel()
        def declareOk = consumerChannel.queueDeclare()
        consumerChannel.queueBind(declareOk.queue, AMQPEventPublisher.EVENT_EXCHANGE, 'Device was registered')

        consumer = new QueueingConsumer(consumerChannel);
        consumerChannel.basicConsume(declareOk.queue, AUTO_ACK, consumer);
    }


    @Test
    public void should_send_a_serialized_event_to_the_message_broker() {
        def deviceId = randomUUID()
        final Event<Device> event = new Device_was_registered(deviceId: deviceId, deviceName: "new device name")

        eventPublisher.publish(new EventEnvelope<Device>('CQRS Core Library', 'Tests', 'Device', deviceId, event))

        assertThat receivedMessage(), equalTo(toJSON(event))
    }


    private String receivedMessage() {
        QueueingConsumer.Delivery delivery = consumer.nextDelivery();
        return new String(delivery.getBody());
    }

    @After
    public void tearDown() throws Exception {
        consumerChannel.close()
        connection.close()
    }
}
