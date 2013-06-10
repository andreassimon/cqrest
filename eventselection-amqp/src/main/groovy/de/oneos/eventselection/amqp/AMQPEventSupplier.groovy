package de.oneos.eventselection.amqp

import com.rabbitmq.client.*

import org.apache.commons.logging.*

import static de.oneos.eventselection.amqp.AMQPConstants.*
import de.oneos.eventstore.*


class AMQPEventSupplier extends DefaultConsumer implements Consumer, EventSupplier {
    static Log log = LogFactory.getLog(AMQPEventSupplier)

    Channel channel
    String queueName
    Collection<EventProcessor> eventProcessors = []

    AMQPEventSupplier(Connection connection) {
        this(connection.createChannel())
    }

    AMQPEventSupplier(Channel channel) {
        super(channel)
        setChannel(channel)
    }

    static String routingKey(Map<String, ?> criteria) {
        criteria.withConstrainedValues(['applicationName', 'boundedContextName', 'aggregateName', 'eventName']) { constrainedValues ->
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

    void setEventProcessors(Collection<EventProcessor> eventProcessors) {
        this.eventProcessors.clear()
        eventProcessors.each { subscribeTo([:], it) }
    }

    @Override
    void subscribeTo(Map<String, ?> criteria, EventProcessor eventProcessor) {
        channel.queueBind(this.queueName, EVENT_EXCHANGE_NAME, routingKey(criteria))
        eventProcessors << eventProcessor
        log.debug "Bound queue '$queueName' to exchange '$EVENT_EXCHANGE_NAME' with routingKey '${routingKey(criteria)}'"
    }

    @Override
    void handleDelivery(String consumerTag, Envelope envelope, AMQP.BasicProperties properties, byte[] body) {
        def eventEnvelope = EventEnvelope.fromJSON(new String(body))

        eventProcessors.each {
            try {
                it.process(eventEnvelope)
                log.debug "Delivered $eventEnvelope to $it"
            } catch(Exception e) {
                log.warn "Exception was raised when processing event '${eventEnvelope.eventName}' ${eventEnvelope.eventAttributes}", e
            }
        }
        channel.basicAck(envelope.deliveryTag, SINGLE_MESSAGE)
    }
}
