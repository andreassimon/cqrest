package readmodels

import com.rabbitmq.client.AMQP;
import com.rabbitmq.client.Channel
import com.rabbitmq.client.ConnectionFactory
import com.rabbitmq.client.QueueingConsumer
import com.rabbitmq.client.impl.AMQConnection
import groovy.json.JsonSlurper
import org.springframework.jdbc.core.JdbcTemplate

import static infrastructure.messaging.AMQPConstants.*

class ReadModelBuilder implements Runnable {
    final JdbcTemplate jdbcTemplate

    ConnectionFactory factory
    AMQConnection connection
    Channel channel
    QueueingConsumer consumer
    JsonSlurper slurper = new JsonSlurper()

    Map<String, Closure> eventHandler


    private ReadModelBuilder(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate

        eventHandler = new DefaultHashMap<String, Closure>({ eventName, _eventAttributes -> System.err.println "Unknown event name: ${eventName}" })
        eventHandler.put 'New device was registered', { jdbc, eventName, eventAttributes ->
            jdbc.update("INSERT INTO DeviceSummary (deviceId, deviceName) VALUES (?, ?);", eventAttributes.deviceId, eventAttributes.deviceName);
        }.curry(jdbcTemplate)
        eventHandler.put 'Device was unregistered', { jdbc, eventName, eventAttributes ->
            jdbc.update("DELETE FROM DeviceSummary WHERE deviceId = ?);", eventAttributes.deviceId);
        }.curry(jdbcTemplate)

        factory = new ConnectionFactory()
        connection = factory.newConnection()
        channel = connection.createChannel()
        final AMQP.Queue.DeclareOk declare = channel.queueDeclare(EVENT_QUEUE, NOT_DURABLE, NOT_EXCLUSIVE, AUTO_DELETE, NO_ADDITIONAL_ARGUMENTS)
        println "Messages in $EVENT_QUEUE: ${declare.messageCount}"
        consumer = new QueueingConsumer(channel)

        channel.basicConsume(EVENT_QUEUE, NO_AUTO_ACK, consumer)
    }

    static Thread start(JdbcTemplate jdbcTemplate) {
        final instance = new Thread(new ReadModelBuilder(jdbcTemplate))
        instance.start()
        return instance
    }


    @Override
    void run() {
        while (!Thread.interrupted()) {
            try {
                QueueingConsumer.Delivery delivery = consumer.nextDelivery();
                def message = new String(delivery.getBody());

                def jsonMap = slurper.parseText(message)

                jsonMap.each { String eventName, eventAttributes ->
                   eventHandler[eventName].call(eventName, eventAttributes)
                }
                channel.basicAck(delivery.envelope.deliveryTag, SINGLE_MESSAGE)
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


