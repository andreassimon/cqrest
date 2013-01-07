package readmodels

import com.rabbitmq.client.*
import groovy.json.JsonSlurper
import org.springframework.jdbc.core.JdbcTemplate
import readmodels.eventhandlers.EventHandler

import static infrastructure.messaging.AMQPConstants.*

class ReadModelBuilder implements Runnable {
    static final String MESSAGE_QUEUE = "event-queue"
    static final long CONSUMER_TIMEOUT = 10

    Thread thread
    JdbcTemplate jdbcTemplate

    Connection connection
    Channel channel

    QueueingConsumer consumer
    JsonSlurper slurper = new JsonSlurper()


    private final Map<String, EventHandler> eventHandlers = new DefaultHashMap<String, EventHandler>(new EventHandler() {

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


    ReadModelBuilder(Connection connection) {

        this.connection = connection
        this.channel = connection.createChannel()

        declareMessageQueue(connection)

        consumer = new QueueingConsumer(channel)

        this.channel = connection.createChannel()
        channel.basicConsume(MESSAGE_QUEUE, NO_AUTO_ACK, consumer)
    }

    void setEventHandlers(List<EventHandler> eventHandlersObjects) {
        eventHandlersObjects.each { eventHandlerObject ->
            eventHandlers[eventHandlerObject.eventName] = eventHandlerObject
        }
    }

    /**
     * TODO Das ist ne Kruecke, weil ich nicht rausgefunden habe, wie ich start() aufrufen kann, wenn es keine Property ist
     * Vielleicht durch ne FactoryBean loesen
     * @param shouldStart
     */
    void setStart(boolean shouldStart) {
        if (shouldStart) {
            start()
        }
    }

    static def declareMessageQueue(Connection connection) {
        withChannel(connection) {
            queueDelete(MESSAGE_QUEUE, ALWAYS, ALWAYS)
        }

        def channel = connection.createChannel()
        final declareOk = channel.queueDeclare(MESSAGE_QUEUE, NOT_DURABLE, EXCLUSIVE, AUTO_DELETE, NO_ADDITIONAL_ARGUMENTS)
        return [declareOk, channel]
    }

    private static void withChannel(Connection connection, Closure channelClosure) {
        def channel = connection.createChannel()
        try {
            channel.with(channelClosure)
        } catch (IOException) {
        } finally {
            try {
                channel.close()
            } catch (AlreadyClosedException) {}
        }
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
                    delivery = consumer.nextDelivery(CONSUMER_TIMEOUT);
                }
                if (delivery) {
                    def message = new String(delivery.getBody());

                    def jsonMap = slurper.parseText(message)

                    jsonMap.each { String eventName, eventAttributes ->
                        eventHandlers[eventName].handleEvent(jdbcTemplate, eventName, eventAttributes)
                    }
                    synchronized (channelLock) {
                        channel.basicAck(delivery.envelope.deliveryTag, SINGLE_MESSAGE)
                    }
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
        closeResource channel
        closeResource connection
    }

    void closeResource(resource) {
        try {
            resource.close()
        } catch (Exception) {
            // catchall
        }
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


