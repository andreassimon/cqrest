package readmodels

import com.rabbitmq.client.Channel
import com.rabbitmq.client.ConnectionFactory
import com.rabbitmq.client.QueueingConsumer
import com.rabbitmq.client.impl.AMQConnection
import groovy.json.JsonSlurper
import infrastructure.messaging.AMQPConstants
import org.springframework.jdbc.core.JdbcTemplate

import static infrastructure.messaging.AMQPConstants.AUTO_ACK
import static infrastructure.messaging.AMQPConstants.EVENT_QUEUE
import static infrastructure.messaging.AMQPConstants.NOT_DURABLE
import static infrastructure.messaging.AMQPConstants.NOT_EXCLUSIVE
import static infrastructure.messaging.AMQPConstants.NO_ADDITIONAL_ARGUMENTS
import static infrastructure.messaging.AMQPConstants.NO_AUTO_DELETE


class ReadModelBuilder implements Runnable {
    final JdbcTemplate jdbcTemplate
    ConnectionFactory factory
    AMQConnection connection
    Channel channel
    QueueingConsumer consumer
    JsonSlurper slurper = new JsonSlurper()

    ReadModelBuilder(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate

        factory = new ConnectionFactory();
        factory.setHost("localhost");
        connection = factory.newConnection();
        channel = connection.createChannel();
        channel.queueDeclare(EVENT_QUEUE, NOT_DURABLE, NOT_EXCLUSIVE, NO_AUTO_DELETE, NO_ADDITIONAL_ARGUMENTS);
        consumer = new QueueingConsumer(channel);

        channel.basicConsume(EVENT_QUEUE, AUTO_ACK, consumer);

        new Thread(this).start()
    }


    @Override
    void run() {
        while (true) {
            def delivery = consumer.nextDelivery();
            def message = new String(delivery.getBody());

            def jsonMap = slurper.parseText(message)

            jsonMap.each { eventName, attributes ->
                jdbcTemplate.update("INSERT INTO DeviceSummary (deviceId, deviceName) VALUES (?, ?);", attributes.deviceId, attributes.deviceName);
            }
//            channel.basicConsume(EVENT_QUEUE, AUTO_ACK, consumer);
//            QueueingConsumer.Delivery delivery = consumer.nextDelivery();
//            def message = new String(delivery.getBody());
//
//            def jsonMap = slurper.parseText(message)
//
//            jsonMap.each { eventName, attributes ->
//                this.jdbcTemplate.update("INSERT INTO DeviceSummary (deviceId, deviceName) VALUES (?, ?);", attributes.deviceId, attributes.desviceName );
//            }
        }
    }
}
