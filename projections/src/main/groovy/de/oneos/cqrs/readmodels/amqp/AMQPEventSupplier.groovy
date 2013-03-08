package de.oneos.cqrs.readmodels.amqp

import de.oneos.cqrs.readmodels.EventFilter
import de.oneos.cqrs.readmodels.EventProcessor
import de.oneos.cqrs.readmodels.EventSupplier
import groovy.json.JsonSlurper
import org.apache.commons.logging.Log
import org.apache.commons.logging.LogFactory
import com.rabbitmq.client.*

import static de.oneos.cqrs.readmodels.amqp.AMQPConstants.*

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
        channel.basicConsume(queueName, NO_AUTO_ACK, this)
        log.debug "Declared queue '$queueName'"
    }

    @Override
    void subscribeTo(EventFilter eventFilter, EventProcessor eventProcessor) {
        channel.queueBind(this.queueName, EVENT_EXCHANGE_NAME, routingKey(eventFilter))
        eventProcessors << eventProcessor
        log.debug "Bound queue '$queueName' to exchange '$EVENT_EXCHANGE_NAME' with routingKey '${routingKey(eventFilter)}'"
    }

    @Override
    void handleDelivery(String consumerTag, Envelope envelope, AMQP.BasicProperties properties, byte[] body) {
        Map deserializedEvent = slurper.parseText(new String(body))

        eventProcessors.each {
            try {
                it.process(deserializedEvent)
                log.debug "Delivered $deserializedEvent to $it"
            } catch(Exception e) {
                log.warn "Exception was raised when processing event '${deserializedEvent.eventName}' ${deserializedEvent.attributes}", e
            }
        }
        channel.basicAck(envelope.deliveryTag, SINGLE_MESSAGE)
    }
}
