package de.oneos.cqrs.readmodels.amqp

import de.oneos.cqrs.readmodels.EventFilter
import de.oneos.cqrs.readmodels.EventProcessor
import de.oneos.cqrs.readmodels.EventSupplier
import com.rabbitmq.client.*
import groovy.json.JsonSlurper
import org.apache.commons.logging.Log
import org.apache.commons.logging.LogFactory

class AMQPEventSupplier extends DefaultConsumer implements Consumer, EventSupplier {
    static Log log = LogFactory.getLog(AMQPEventSupplier)

    JsonSlurper slurper = new JsonSlurper()

    Channel channel
    String queueName
    List<EventProcessor> eventProcessors = []

    AMQPEventSupplier(Connection connection) {
        this(connection.createChannel())
    }

    AMQPEventSupplier(Channel channel) {
        super(channel)
        setChannel(channel)
    }

    static String routingKey(EventFilter eventFilter) {
        eventFilter.withConstrainedValues(['applicationName', 'boundedContextName', 'aggregateName', 'eventName']) { constrainedValues ->
            constrainedValues.collect { it ?: '*' }.join('.')
        }
    }

    void setChannel(Channel channel) {
        this.channel = channel
        declareQueue(channel)
    }

    void declareQueue(Channel channel) {
        def declareOk = channel.queueDeclare()
        this.queueName = declareOk.queue
        channel.basicConsume(queueName, AMQPConstants.NO_AUTO_ACK, this)
    }

    @Override
    void subscribeTo(EventFilter eventFilter, EventProcessor eventProcessor) {
        channel.queueBind(this.queueName, AMQPConstants.EVENT_EXCHANGE_NAME, routingKey(eventFilter))
        eventProcessors << eventProcessor
    }

    @Override
    void handleDelivery(String consumerTag, Envelope envelope, AMQP.BasicProperties properties, byte[] body) {
        Map deserializedEvent = slurper.parseText(new String(body))

        try {
            eventProcessors.each {
                it.process(deserializedEvent)
            }
        } catch (Exception e) {
            log.info "Exception was raised when handling event '${deserializedEvent.eventName}' ${deserializedEvent.attributes}", e
        }
        channel.basicAck(envelope.deliveryTag, AMQPConstants.SINGLE_MESSAGE)
    }
}
