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

    // The upstream parameter is only necessary to guarantee successful setup of the message pipelines
    AMQPEventSupplier(Connection connection, AMQPEventPublisher upstream) {
        this.channel = connection.createChannel()
    }

    static String routingKey(Map<String, ?> criteria) {
        criteria.subMap(['applicationName', 'boundedContextName', 'aggregateName', 'eventName']).values().collect {
            it ?: '*'
        }.join('.')
    }


    @Override
    void subscribeTo(Map<String, ?> criteria, EventConsumer eventConsumer) {
        deliverEvents(criteria) { EventEnvelope eventEnvelope ->
            eventConsumer.process(eventEnvelope)
            log.debug "Delivered $eventEnvelope to $eventConsumer"
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
