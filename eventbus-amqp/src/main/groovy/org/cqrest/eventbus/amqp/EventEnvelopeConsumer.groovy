package org.cqrest.eventbus.amqp

import org.apache.commons.logging.*
import com.rabbitmq.client.*

import static AMQP.*
import org.cqrest.eventsourcing.*


class EventEnvelopeConsumer extends DefaultConsumer implements Consumer {
    public static Log log = LogFactory.getLog(EventEnvelopeConsumer)

    Closure block

    EventEnvelopeConsumer(Channel channel, Closure block) {
        super(channel)
        this.block = block
    }

    @Override
    void handleDelivery(String consumerTag, Envelope envelope, com.rabbitmq.client.AMQP.BasicProperties properties, byte[] body) {
        def eventEnvelope
        try {
            eventEnvelope = EventEnvelope.fromJSON(new String(body))
            block.call(eventEnvelope)
        } catch(Exception e) {
            if(eventEnvelope) {
                log.warn "${e.getClass().getCanonicalName()} was raised when processing event envelope '${eventEnvelope.eventName}' ${eventEnvelope.eventAttributes}", e
            } else {
                log.warn "${e.getClass().getCanonicalName()} was raised when parsing event envelope", e
            }
        }
        try {
            getChannel().basicAck(envelope.deliveryTag, SINGLE_MESSAGE)
        } catch(AlreadyClosedException e) {
            log.info "Consumer ${toString()} for channel ${getChannel()} threw ${e.getClass().canonicalName}: ${e.message}"
        }
    }

}
