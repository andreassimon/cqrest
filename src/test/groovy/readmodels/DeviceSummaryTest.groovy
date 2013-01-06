package readmodels

import com.rabbitmq.client.*
import groovy.json.*
import org.junit.*
import org.springframework.jdbc.core.JdbcTemplate

import static infrastructure.messaging.AMQPConstants.*
import static org.junit.Assert.*
import static org.mockito.Mockito.*

class DeviceSummaryTest {
    Connection producerConnection
    Channel producerChannel

    final def NEW_DEVICE_ID = 'e5270db1-2e83-4499-b8f5-91d0491d9fce'
    final def NEW_DEVICE_NAME = 'new device name'

    final def REGISTERED_DEVICE_ID = '7083f2f5-3131-462a-a24a-eb5253f8227b'
    JsonSlurper slurper = new JsonSlurper()

    JdbcTemplate jdbcTemplate = mock(JdbcTemplate.class)
    ReadModelBuilder readModelBuilder

    @Before
    public void setUp() throws Exception {
        def connectionFactory = new ConnectionFactory()

        producerConnection = connectionFactory.newConnection()
        producerChannel = producerConnection.createChannel()

        readModelBuilder = ReadModelBuilder.newInstance(jdbcTemplate)
        readModelBuilder.purgeQueue()
        readModelBuilder.start()
    }

    @Test
    public void should_create_a_new_record_for_a_new_device() {

        publishEvent(
                ('New device was registered'): [
                        deviceId: NEW_DEVICE_ID,
                        deviceName: NEW_DEVICE_NAME
                ])

        verify(jdbcTemplate).update("INSERT INTO DeviceSummary (deviceId, deviceName) VALUES (?, ?);", UUID.fromString(NEW_DEVICE_ID), NEW_DEVICE_NAME)
    }

    @Test
    public void should_delete_record_for_a_registered_device() {
        publishEvent(
                ('Device was unregistered'): [
                        deviceId: REGISTERED_DEVICE_ID
                ])

        verify(jdbcTemplate).update("DELETE FROM DeviceSummary WHERE deviceId = ?;", REGISTERED_DEVICE_ID.toString())
    }

    @Test
    public void should_mark_device_locked() {
        publishEvent(
                ('Device was locked out'): [
                        deviceId: REGISTERED_DEVICE_ID
                ])

        verify(jdbcTemplate).update("UPDATE DeviceSummary SET locked = true WHERE deviceid = ?;", REGISTERED_DEVICE_ID.toString())
    }

    private void publishEvent(Map<String, Object> eventAttributes) {
        final json = toJSON(eventAttributes).bytes
        producerChannel.basicPublish(DEFAULT_EXCHANGE, ReadModelBuilder.MESSAGE_QUEUE, NO_PROPERTIES, json)
        Thread.sleep(100)
        readModelBuilder.interrupt()
    }

    private String toJSON(Map<String, Object> json) {
        new JsonBuilder(json).toString()
    }

    @After
    public void tearDown() throws Exception {
        close producerChannel
        close producerConnection
    }

    void close(amqpResource) {
       try {
           amqpResource.close()
       } catch (AlreadyClosedException) { }
    }

}
