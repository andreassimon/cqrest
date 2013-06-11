package de.oneos.eventselection.amqp

import com.rabbitmq.client.*
import de.oneos.eventstore.*
import org.apache.commons.logging.*

import static de.oneos.eventselection.amqp.AMQPConstants.*

class AMQPEventPublisher implements EventProcessor {
    static Log log = LogFactory.getLog(AMQPEventPublisher)

    Channel channel

    AMQPEventPublisher(Connection connection) {
        channel = connection.createChannel()
        channel.exchangeDeclare(EVENT_EXCHANGE_NAME, TOPIC_EXCHANGE)
        channel.exchangeDeclare(EVENT_QUERY_EXCHANGE_NAME, DIRECT_EXCHANGE)
    }

    @Override
    void process(EventEnvelope eventEnvelope) throws IllegalAmqpEventCoordinate {
        def routingKey = routingKey(eventEnvelope)
        try {
            channel.basicPublish EVENT_EXCHANGE_NAME, routingKey, NO_PROPERTIES, eventEnvelope.toJSON().bytes
            log.debug "Published ${eventEnvelope.toJSON()} to '$EVENT_EXCHANGE_NAME' with routingKey '${routingKey}'"
        } catch(Exception e) {
            log.warn "Exception during publishing $eventEnvelope to $EVENT_EXCHANGE_NAME", e
        }
    }

    @Override
    void wasRegisteredAt(EventSupplier eventSupplier) {
        AMQP.Queue.DeclareOk declareOk = channel.queueDeclare()
        channel.queueBind(declareOk.queue, EVENT_QUERY_EXCHANGE_NAME, EVENT_QUERY)
        channel.basicConsume(declareOk.queue, new EventQueryConsumer(channel, eventSupplier))
        log.debug("Bound event query queue '$declareOk.queue' to event supplier '$eventSupplier'")
    }

    private static routingKey(EventEnvelope eventEnvelope) throws IllegalAmqpEventCoordinate {
        def eventCoordinates = ['applicationName', 'boundedContextName', 'aggregateName', 'eventName'].collect {
            eventEnvelope[it]
        }
        if(eventCoordinates.find { it.contains('.') }) {
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
