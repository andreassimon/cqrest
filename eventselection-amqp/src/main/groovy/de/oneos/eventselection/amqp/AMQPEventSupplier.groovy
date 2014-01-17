package de.oneos.eventselection.amqp

import groovy.json.*
import org.apache.commons.logging.*

import com.rabbitmq.client.*

import static AMQP.*

import de.oneos.eventsourcing.EventConsumer
import de.oneos.eventsourcing.EventEnvelope
import de.oneos.eventsourcing.EventSupplier


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
        eventConsumers.each { subscribeTo(it) }
    }

    @Override
    EventSupplier rightShift(EventConsumer eventConsumer) {
        subscribeTo(eventConsumer)
        return this
    }

    @Override
    void subscribeTo(EventConsumer eventConsumer) {
        subscribeTo(eventConsumer.eventCriteria, eventConsumer)
    }

    @Override
    void subscribeTo(Map<String, ?> criteria, EventConsumer eventConsumer) {
        deliverEvents(criteria) { EventEnvelope eventEnvelope ->
            eventConsumer.process(eventEnvelope)
            log.debug "Delivered $eventEnvelope to $eventConsumer"
        }

        eventConsumers << eventConsumer
        try {
            eventConsumer.wasRegisteredAt(this)
        } catch(e) {
            eventConsumers.remove(eventConsumer)
            log.warn "'$e.message' during subscription of $eventConsumer", e
        }
    }


    protected void deliverEvents(Map<String, ? extends Object> criteria, Closure callback) {
        String eventEnvelopeQueue = consumeQueue(channel, new EventEnvelopeConsumer(channel, callback))
        channel.queueBind(eventEnvelopeQueue, EVENT_EXCHANGE_NAME, routingKey(criteria))
        log.debug "Bound queue '${eventEnvelopeQueue}' to exchange '$EVENT_EXCHANGE_NAME' with routingKey '${routingKey(criteria)}'"
    }

    @Override
    void withEventEnvelopes(Map<String, ?> criteria, Closure block) {
        String eventEnvelopeQueue = consumeQueue(channel, new EventEnvelopeConsumer(channel, block))
        channel.basicPublish(EVENT_QUERY_EXCHANGE_NAME, EVENT_QUERY, replyTo(eventEnvelopeQueue), new JsonBuilder(criteria).toString().bytes)
        log.debug "Queried for $criteria at '${eventEnvelopeQueue}'"
    }

}
