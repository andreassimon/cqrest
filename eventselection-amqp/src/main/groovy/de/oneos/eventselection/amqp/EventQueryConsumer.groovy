package de.oneos.eventselection.amqp

import groovy.json.*
import org.apache.commons.logging.*

import com.rabbitmq.client.Channel
import com.rabbitmq.client.Consumer
import com.rabbitmq.client.DefaultConsumer
import com.rabbitmq.client.Envelope

import de.oneos.eventsourcing.EventStream
import de.oneos.eventsourcing.EventSupplier


class EventQueryConsumer extends DefaultConsumer implements Consumer {
    static Log log = LogFactory.getLog(EventQueryConsumer)

    JsonSlurper parser = new JsonSlurper()
    @Deprecated
    EventSupplier eventSupplier
    EventStream eventSource

    @Deprecated
    EventQueryConsumer(Channel channel, EventSupplier eventSupplier) {
        super(channel)
        this.eventSupplier = eventSupplier
    }

    EventQueryConsumer(Channel channel, EventStream eventSource) {
        super(channel)
        setEventSource(eventSource)
    }

    void setEventSource(EventStream eventSource) {
        assert eventSource
        this.eventSource = eventSource
    }

    @Override
    void handleDelivery(String consumerTag, Envelope envelope, com.rabbitmq.client.AMQP.BasicProperties properties, byte[] body) {
        final Map criteria = parser.parseText(new String(body)) as Map
        final addressee = properties.replyTo
        log.debug("$this received query for $criteria to deliver to ${addressee}")

        try {
            eventSource.observe(criteria).subscribe(new EventStreamSender(addressee, envelope.deliveryTag, super.channel))
        } catch(Exception e) {
            log.warn "${e.getClass().getCanonicalName()} was raised when publishing event query results for '$criteria'", e
        }
    }

}
