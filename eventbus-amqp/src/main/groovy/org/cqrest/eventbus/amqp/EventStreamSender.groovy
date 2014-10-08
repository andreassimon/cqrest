package org.cqrest.eventbus.amqp

import org.apache.commons.logging.Log
import org.apache.commons.logging.LogFactory

import com.rabbitmq.client.Channel

import org.cqrest.eventsourcing.EventEnvelope

import java.util.concurrent.atomic.AtomicBoolean


class EventStreamSender implements org.cqrest.reactive.Observer<EventEnvelope> {
    public static Log log = LogFactory.getLog(EventStreamSender)

    private final long deliveryTag
    private final String addressee
    private final Channel channel

    private final AtomicBoolean acknowledgedReception = new AtomicBoolean(false)

    EventStreamSender(String addressee, long deliveryTag, Channel channel) {
        assert addressee
        assert channel

        this.addressee = addressee
        this.deliveryTag = deliveryTag
        this.channel = channel
    }

    @Override
    void onCompleted() {
        // TODO implement
        log.warn "EventStreamSender#onCompleted is not implemented properly! Do it! NOW!"
        log.debug "[$this] Event stream was completed"
    }

    @Override
    void onError(Throwable e) {
        // TODO implement
        log.warn "EventStreamSender#onError is not implemented properly! Do it! NOW!"
        log.warn "${e.getClass().getCanonicalName()} was raised when sending event stream to '$addressee'", e
    }

    @Override
    void onNext(EventEnvelope eventEnvelope) {
        synchronized(acknowledgedReception) {
            if(acknowledgedReception.compareAndSet(false, true)) {
                channel.basicAck(deliveryTag, AMQP.SINGLE_MESSAGE)
            }
        }
        log.debug("Publishing $eventEnvelope to ${addressee}")
        channel.basicPublish('', addressee, AMQP.NO_PROPERTIES, eventEnvelope.toJSON().bytes)
    }

}
