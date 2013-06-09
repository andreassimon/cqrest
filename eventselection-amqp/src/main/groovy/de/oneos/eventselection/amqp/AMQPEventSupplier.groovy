package de.oneos.eventselection.amqp

import com.rabbitmq.client.*
import de.oneos.eventselection.*
import groovy.json.*
import org.apache.commons.logging.*

import static de.oneos.eventselection.amqp.AMQPConstants.*
import de.oneos.eventstore.*


class AMQPEventSupplier extends DefaultConsumer implements Consumer, EventSupplier {
    static Log log = LogFactory.getLog(AMQPEventSupplier)

    JsonSlurper slurper = new JsonSlurper()

    Channel channel
    String queueName
    List<EventPublisher> eventPublishers = []

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
    void subscribeTo(EventFilter eventFilter, EventPublisher eventPublisher) {
        channel.queueBind(this.queueName, EVENT_EXCHANGE_NAME, routingKey(eventFilter))
        eventPublishers << eventPublisher
        log.debug "Bound queue '$queueName' to exchange '$EVENT_EXCHANGE_NAME' with routingKey '${routingKey(eventFilter)}'"
    }

    @Override
    void handleDelivery(String consumerTag, Envelope envelope, AMQP.BasicProperties properties, byte[] body) {
        def eventEnvelope = EventEnvelope.fromJSON(new String(body))

        eventPublishers.each {
            try {
                it.publish(eventEnvelope)
                log.debug "Delivered $eventEnvelope to $it"
            } catch(Exception e) {
                log.warn "Exception was raised when processing event '${eventEnvelope.eventName}' ${eventEnvelope.eventAttributes}", e
            }
        }
        channel.basicAck(envelope.deliveryTag, SINGLE_MESSAGE)
    }
}
