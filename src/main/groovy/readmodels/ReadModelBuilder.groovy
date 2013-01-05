package readmodels

import com.rabbitmq.client.Channel
import com.rabbitmq.client.Connection
import com.rabbitmq.client.ConnectionFactory
import com.rabbitmq.client.QueueingConsumer
import groovy.json.JsonSlurper
import org.springframework.jdbc.core.JdbcTemplate

import static infrastructure.messaging.AMQPConstants.*

class ReadModelBuilder implements Runnable {
    public static final String MESSAGE_QUEUE = "event-queue"
    Thread thread
    final JdbcTemplate jdbcTemplate

    Connection connection
    Channel channel

    QueueingConsumer consumer
    JsonSlurper slurper = new JsonSlurper()

    Map<String, Closure> eventHandlers

    final String channelLock = 'channel-lock'


    private ReadModelBuilder(JdbcTemplate jdbcTemplate, Connection connection, Channel channel) {
        this.jdbcTemplate = jdbcTemplate
        this.connection = connection
        this.channel = channel


        eventHandlers = new DefaultHashMap<String, Closure>({ eventName, _eventAttributes -> System.err.println "Unknown event name: ${eventName}" })
        eventHandlers.put 'New device was registered', { jdbc, eventName, eventAttributes ->
            jdbc.update("INSERT INTO DeviceSummary (deviceId, deviceName) VALUES (?, ?);", eventAttributes.deviceId, eventAttributes.deviceName);
        }.curry(jdbcTemplate)
        eventHandlers.put 'Device was unregistered', { jdbc, eventName, eventAttributes ->
            jdbc.update("DELETE FROM DeviceSummary WHERE deviceId = ?;", eventAttributes.deviceId);
        }.curry(jdbcTemplate)
        eventHandlers.put 'Device was locked out', { jdbc, eventName, eventAttributes ->
            jdbc.update("UPDATE DeviceSummary SET locked = true WHERE deviceid = ?;", eventAttributes.deviceId);
        }.curry(jdbcTemplate)

        consumer = new QueueingConsumer(channel)

        channel.basicConsume(MESSAGE_QUEUE, NO_AUTO_ACK, consumer)
    }

    static def declareMessageQueue(Connection connection, Channel channel) {
        try {
            channel.queueDelete(MESSAGE_QUEUE, ALWAYS, ALWAYS)
        } catch(IOException) {
        } finally {
            try {
                channel.close()
            } catch (AlreadyClosedException) { }
            channel = connection.createChannel()
        }
        final declareOk = channel.queueDeclare(MESSAGE_QUEUE, NOT_DURABLE, EXCLUSIVE, AUTO_DELETE, NO_ADDITIONAL_ARGUMENTS)
        return [declareOk, channel]
    }

    static ReadModelBuilder newInstance(JdbcTemplate jdbcTemplate) {
        def factory = new ConnectionFactory()
        def connection = factory.newConnection()
        def channel = connection.createChannel()
        declareMessageQueue(connection, channel)

        channel = connection.createChannel()
        return new ReadModelBuilder(jdbcTemplate, connection, channel)
    }

    void start() {
        thread = new Thread(this)
        thread.start()
    }


    def interrupt() {
        thread.interrupt()
    }


    def purgeQueue() {
        synchronized (channelLock) {
            channel.queuePurge(MESSAGE_QUEUE)
        }
    }

    @Override
    void run() {
        while (!Thread.interrupted()) {
            try {
                QueueingConsumer.Delivery delivery
                synchronized (channelLock) {
                    delivery = consumer.nextDelivery();
                }
                def message = new String(delivery.getBody());

                def jsonMap = slurper.parseText(message)

                jsonMap.each { String eventName, eventAttributes ->
                   eventHandlers[eventName].call(eventName, eventAttributes)
                }
                synchronized (channelLock) {
                    channel.basicAck(delivery.envelope.deliveryTag, SINGLE_MESSAGE)
                }

                if (Thread.interrupted()) {
                    throw new InterruptedException()
                }
            } catch (InterruptedException e) {
                closeResources()
                break
            } catch (e) {
                e.printStackTrace(System.err)
            }
        }
    }

    void closeResources() {
        channel.close()
        connection.close()
    }

    static class DefaultHashMap<K, V> extends HashMap<K, V> {
        V defaultValue;

        public DefaultHashMap(V defaultValue) {
            this.defaultValue = defaultValue;
        }

        @Override
        public V get(K k) {
            if (!this.containsKey(k)) {
                return this.defaultValue
            }
            return super.get(k);
        }
    }
}


