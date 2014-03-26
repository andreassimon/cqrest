package de.oneos.eventselection.amqp

import org.apache.commons.logging.*
import com.rabbitmq.client.*


import de.oneos.eventsourcing.EventConsumer
import de.oneos.eventsourcing.EventEnvelope
import de.oneos.eventsourcing.EventSupplier

import static AMQP.*


class AMQPEventPublisher implements EventConsumer {
    static Log log = LogFactory.getLog(AMQPEventPublisher)

    Channel channel

    AMQPEventPublisher(Connection connection, EventSupplier upstream) {
        channel = connection.createChannel()
        try {
            channel.exchangeDeclare(EVENT_EXCHANGE_NAME, TOPIC_EXCHANGE, DURABLE, NO_AUTO_DELETE, PUBLIC, [:])
        } catch(IOException e) {
            log.info(e.cause.message, e)
            channel = connection.createChannel()
        }
        channel.exchangeDeclare(EVENT_QUERY_EXCHANGE_NAME, DIRECT_EXCHANGE)
        wasRegisteredAt(upstream)
    }

    @Override
    void process(EventEnvelope eventEnvelope) throws IllegalAmqpEventCoordinate {
        def routingKey = routingKey(eventEnvelope)
        try {
            channel.basicPublish EVENT_EXCHANGE_NAME, routingKey, NO_PROPERTIES, eventEnvelope.toJSON().bytes
            if(log.isDebugEnabled()) {
                log.debug "Published ${eventEnvelope.toJSON()} to '$EVENT_EXCHANGE_NAME' with routingKey '${routingKey}'"
            }
        } catch(Exception e) {
            log.warn "Exception during publishing $eventEnvelope to $EVENT_EXCHANGE_NAME", e
        }
    }

    @Override
    void wasRegisteredAt(EventSupplier eventSupplier) {
        assert eventSupplier != null

        com.rabbitmq.client.AMQP.Queue.DeclareOk declareOk = channel.queueDeclare()
        channel.queueBind(declareOk.queue, EVENT_QUERY_EXCHANGE_NAME, EVENT_QUERY)
        channel.basicConsume(declareOk.queue, new EventQueryConsumer(channel, eventSupplier))
        log.debug("Bound event query queue '$declareOk.queue' to event supplier '$eventSupplier'")

        try {
            eventSupplier.subscribeTo([:], this)
        } catch(e) {
            log.warn("${e.getClass().getCanonicalName()} was thrown when subscribing $this to $eventSupplier", e)
        }
    }


    private static routingKey(EventEnvelope eventEnvelope) throws IllegalAmqpEventCoordinate {
        def eventCoordinates = ['applicationName', 'boundedContextName', 'aggregateName', 'eventName'].collect {
            eventEnvelope[it] as String
        }
        if(eventCoordinates.any { it.contains('.') }) {
            throw new IllegalAmqpEventCoordinate(eventCoordinates)
        }
        eventCoordinates.join('.')
    }

    // TODO finalizing lernen!!!
    @Override
    void finalize() throws Throwable {
        try {
            channel.close()
        } catch (Exception) {
        } finally {
            super.finalize()
        }
    }
}
