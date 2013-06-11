package de.oneos.eventselection.amqp

import org.apache.commons.logging.*
import com.rabbitmq.client.*

import static de.oneos.eventselection.amqp.AMQPConstants.*
import de.oneos.eventstore.*


class EventEnvelopeConsumer extends DefaultConsumer implements Consumer {
    static Log log = LogFactory.getLog(EventEnvelopeConsumer)

    Closure block

    EventEnvelopeConsumer(Channel channel, Closure block) {
        super(channel)
        this.block = block
    }

    @Override
    void handleDelivery(String consumerTag, Envelope envelope, AMQP.BasicProperties properties, byte[] body) {
        def eventEnvelope = EventEnvelope.fromJSON(new String(body))

        try {
            block.call(eventEnvelope)
        } catch(Exception e) {
            log.warn "Exception was raised when processing event envelope '${eventEnvelope.eventName}' ${eventEnvelope.eventAttributes}", e
        }
        super.channel.basicAck(envelope.deliveryTag, SINGLE_MESSAGE)
    }

}
