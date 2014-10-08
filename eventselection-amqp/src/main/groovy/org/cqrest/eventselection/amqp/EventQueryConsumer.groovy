package org.cqrest.eventselection.amqp

import groovy.json.*
import org.apache.commons.logging.*
import com.rabbitmq.client.Channel
import com.rabbitmq.client.Consumer
import com.rabbitmq.client.DefaultConsumer
import com.rabbitmq.client.Envelope

import org.cqrest.eventsourcing.EventEnvelope
import org.cqrest.eventsourcing.EventSupplier

import static AMQP.*


class EventQueryConsumer extends DefaultConsumer implements Consumer {
    static Log log = LogFactory.getLog(EventQueryConsumer)

    JsonSlurper parser = new JsonSlurper()
    EventSupplier eventSupplier

    EventQueryConsumer(Channel channel, EventSupplier eventSupplier) {
        super(channel)
        this.eventSupplier = eventSupplier
    }

    @Override
    void handleDelivery(String consumerTag, Envelope envelope, com.rabbitmq.client.AMQP.BasicProperties properties, byte[] body) {
        final Map criteria = parser.parseText(new String(body)) as Map
        final addressee = properties.replyTo
        log.debug("$this received query for $criteria to deliver to ${addressee}")

        try {
            eventSupplier.withEventEnvelopes(criteria) { EventEnvelope eventEnvelope ->
                log.debug("Publishing $eventEnvelope to ${addressee}")
                super.channel.basicPublish('', addressee, NO_PROPERTIES, eventEnvelope.toJSON().bytes)
            }
        } catch(Exception e) {
            log.warn "${e.getClass().getCanonicalName()} was raised when publishing event query results for '$criteria'", e
        }
        super.channel.basicAck(envelope.deliveryTag, SINGLE_MESSAGE)
    }

}
