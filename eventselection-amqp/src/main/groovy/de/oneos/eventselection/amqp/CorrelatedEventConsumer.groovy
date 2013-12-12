package de.oneos.eventselection.amqp

import com.rabbitmq.client.*
import de.oneos.eventsourcing.Correlation
import org.apache.commons.logging.*

import static AMQP.CORRELATED_EVENT_EXCHANGE
import static AMQP.SINGLE_MESSAGE
import static AMQP.consumeQueue


class CorrelatedEventConsumer extends DefaultConsumer implements Consumer {
    static Log log = LogFactory.getLog(CorrelatedEventConsumer)


    private Correlation correlation

    CorrelatedEventConsumer(Channel channel, Correlation correlation) {
        super(channel)
        // TODO reference by WeakReference
        this.correlation = correlation
        String eventEnvelopeQueue = consumeQueue(channel, this)
        channel.queueBind(eventEnvelopeQueue, CORRELATED_EVENT_EXCHANGE, correlation.routingKey)
    }

    @Override
    void handleDelivery(String consumerTag, Envelope envelope, AMQP.BasicProperties properties, byte[] body) {
        final eventType = new String(body)

        try {
            log.debug "Delivering '$eventType' to $correlation"
            correlation.consumeTriggeredEvent(eventType)
        } catch(Exception e) {
            log.warn "Exception was raised when delivering correlated event '$eventType'", e
        }
        super.channel.basicAck(envelope.deliveryTag, SINGLE_MESSAGE)
    }

}
