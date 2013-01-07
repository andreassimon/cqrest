package readmodels

import com.rabbitmq.client.Channel
import com.rabbitmq.client.Connection
import com.rabbitmq.client.ConnectionFactory
import com.rabbitmq.client.QueueingConsumer
import groovy.json.JsonSlurper
import org.springframework.dao.DataAccessException
import org.springframework.jdbc.core.JdbcTemplate
import readmodels.eventhandlers.EventHandler

import static infrastructure.messaging.AMQPConstants.*

class ReadModelBuilder implements Runnable {
    public static final String MESSAGE_QUEUE = "event-queue"
    Thread thread
    final JdbcTemplate jdbcTemplate

    Connection connection
    Channel channel

    QueueingConsumer consumer
    JsonSlurper slurper = new JsonSlurper()

    Map<String, EventHandler> eventHandlers = new DefaultHashMap<String, EventHandler>(new EventHandler() {

        @Override
        String getEventName() {
            return null
        }

        @Override
        void handleEvent(JdbcTemplate jdbcTemplate, eventName, eventAttributes) {
            System.err.println "Unknown event name: ${eventName}"
        }
    } )

    final String channelLock = 'channel-lock'


    private ReadModelBuilder(JdbcTemplate jdbcTemplate, Connection connection, Channel channel) {
        this.jdbcTemplate = jdbcTemplate
        this.connection = connection
        this.channel = channel

        consumer = new QueueingConsumer(channel)

        channel.basicConsume(MESSAGE_QUEUE, NO_AUTO_ACK, consumer)
    }

    def setEventHandlers(List<EventHandler> eventHandlersObjects) {
        eventHandlersObjects.each { eventHandlerObject ->
            eventHandlers[eventHandlerObject.eventName] = eventHandlerObject
        }
    }

    static def declareMessageQueue(Connection connection, Channel channel) {
        try {
            channel.queueDelete(MESSAGE_QUEUE, ALWAYS, ALWAYS)
        } catch (IOException) {
        } finally {
            try {
                channel.close()
            } catch (AlreadyClosedException) {}
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
        thread = new Thread(this, 'read-model-builder')
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
                    eventHandlers[eventName].handleEvent(jdbcTemplate, eventName, eventAttributes)
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


