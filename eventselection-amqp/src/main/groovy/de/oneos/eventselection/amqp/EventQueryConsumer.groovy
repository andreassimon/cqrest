package de.oneos.eventselection.amqp

import groovy.json.*
import org.apache.commons.logging.*
import com.rabbitmq.client.*

import de.oneos.eventstore.*
import static de.oneos.AMQP.*


class EventQueryConsumer extends DefaultConsumer implements Consumer {
    static Log log = LogFactory.getLog(EventQueryConsumer)

    JsonSlurper parser = new JsonSlurper()
    EventSupplier eventSupplier

    EventQueryConsumer(Channel channel, EventSupplier eventSupplier) {
        super(channel)
        this.eventSupplier = eventSupplier
    }

    @Override
    void handleDelivery(String consumerTag, Envelope envelope, AMQP.BasicProperties properties, byte[] body) {
        Map criteria = parser.parseText(new String(body)) as Map

        try {
            eventSupplier.withEventEnvelopes(criteria) { EventEnvelope eventEnvelope ->
                super.channel.basicPublish('', properties.replyTo, NO_PROPERTIES, eventEnvelope.toJSON().bytes)
            }
        } catch(Exception e) {
            log.warn "Exception was raised when publishing event query results for '$criteria'", e
        }
        super.channel.basicAck(envelope.deliveryTag, SINGLE_MESSAGE)
    }

}
