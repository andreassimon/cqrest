package readmodels

import com.rabbitmq.client.Channel
import com.rabbitmq.client.Connection
import com.rabbitmq.client.ConnectionFactory
import com.rabbitmq.client.QueueingConsumer
import groovy.json.JsonBuilder
import groovy.json.JsonSlurper
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.springframework.jdbc.core.JdbcTemplate

import static infrastructure.messaging.AMQPConstants.*
import static org.mockito.Mockito.mock
import static org.mockito.Mockito.verify

class DeviceSummaryTest {
    ConnectionFactory connectionFactory
    Connection producerConnection, consumerConnection
    Channel producerChannel, consumerChannel

    def eventName = 'New device was registered'
    def newDeviceId = UUID.randomUUID()
    def newDeviceName = 'new device name'

    QueueingConsumer consumer
    QueueingConsumer.Delivery delivery

    JsonSlurper slurper = new JsonSlurper()

    def jdbcTemplate = mock(JdbcTemplate.class)

    @Before
    public void setUp() throws Exception {
        connectionFactory = new ConnectionFactory()

        producerConnection = connectionFactory.newConnection()
        consumerConnection = connectionFactory.newConnection()

        producerChannel = producerConnection.createChannel()
        consumerChannel = consumerConnection.createChannel()

        consumer = new QueueingConsumer(consumerChannel);

        producerChannel.queueDeclare(EVENT_QUEUE, NOT_DURABLE, NOT_EXCLUSIVE, NO_AUTO_DELETE, NO_ADDITIONAL_ARGUMENTS);
    }

    @Test
    public void should_create_a_new_record_for_a_new_device() {
        def readModelBuilder = new ReadModelBuilder(jdbcTemplate)

        publishEvent(
            (eventName): [
                deviceId: newDeviceId,
                deviceName: newDeviceName
            ])

        verify(jdbcTemplate).update("INSERT INTO DeviceSummary (deviceId, deviceName) VALUES (?, ?);", newDeviceId.toString(), newDeviceName)
    }

    private void publishEvent(Map<String, Object> eventAttributes) {
        final json = toJSON(eventAttributes).bytes
        producerChannel.basicPublish('', EVENT_QUEUE, NO_PROPERTIES, json)
        producerChannel.waitForConfirms()
    }

    private String toJSON(Map<String, Object> json) {
        new JsonBuilder(json).toString()
    }

    @After
    public void tearDown() throws Exception {
        producerChannel.close()
        producerConnection.close()

        consumerChannel.close()
        consumerConnection.close()
    }

}
