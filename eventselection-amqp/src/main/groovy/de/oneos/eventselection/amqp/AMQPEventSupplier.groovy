package de.oneos.eventselection.amqp

import groovy.json.*
import org.apache.commons.logging.*

import com.rabbitmq.client.*

import static de.oneos.eventselection.amqp.AMQPConstants.*
import de.oneos.eventstore.*


class AMQPEventSupplier implements EventSupplier {
    static Log log = LogFactory.getLog(AMQPEventSupplier)

    Channel channel
    Collection<EventConsumer> eventConsumers = []

    AMQPEventSupplier(Connection connection) {
        this.channel = connection.createChannel()
    }

    static String routingKey(Map<String, ?> criteria) {
        criteria.subMap(['applicationName', 'boundedContextName', 'aggregateName', 'eventName']).values().collect {
            it ?: '*'
        }.join('.')
    }

    void setEventConsumers(Collection<EventConsumer> eventConsumers) {
        this.eventConsumers.clear()
        eventConsumers.each { subscribeTo([:], it) }
    }

    @Override
    void subscribeTo(Map<String, ?> criteria, EventConsumer eventConsumer) {
        deliverEvents(criteria) { EventEnvelope eventEnvelope ->
            eventConsumer.process(eventEnvelope)
            log.debug "Delivered $eventEnvelope to $eventConsumer"
        }

        eventConsumers << eventConsumer
        eventConsumer.wasRegisteredAt(this)
    }

    protected void deliverEvents(Map<String, ? extends Object> criteria, Closure callback) {
        String eventEnvelopeQueue = createEventEnvelopeQueue(callback)
        channel.queueBind(eventEnvelopeQueue, EVENT_EXCHANGE_NAME, routingKey(criteria))
        log.debug "Bound queue '${eventEnvelopeQueue}' to exchange '$EVENT_EXCHANGE_NAME' with routingKey '${routingKey(criteria)}'"
    }

    protected String createEventEnvelopeQueue(Closure callback) {
        def declareOk = channel.queueDeclare()
        String queueName = declareOk.queue
        log.debug "Declared event envelope queue '${queueName}'"
        channel.basicConsume(queueName, NO_AUTO_ACK, new EventEnvelopeConsumer(channel, callback))
        return queueName
    }

    @Override
    void withEventEnvelopes(Map<String, ?> criteria, Closure block) {
        String eventEnvelopeQueue = createEventEnvelopeQueue(block)
        channel.basicPublish(EVENT_QUERY_EXCHANGE_NAME, EVENT_QUERY, replyTo(eventEnvelopeQueue), new JsonBuilder(criteria).toString().bytes)
        log.debug "Queried for $criteria at '${eventEnvelopeQueue}'"
    }

}
