package de.oneos.eventselection.amqp

import org.apache.commons.logging.*
import com.rabbitmq.client.*
import de.oneos.eventstore.*

import static de.oneos.eventselection.amqp.AMQPConstants.*


class AMQPEventPublisher implements EventPublisher {
    static Log log = LogFactory.getLog(AMQPEventPublisher)

    Channel channel

    AMQPEventPublisher(Connection connection) {
        channel = connection.createChannel()
        channel.exchangeDeclare(EVENT_EXCHANGE_NAME, TOPIC_EXCHANGE)
    }

    @Override
    void publish(EventEnvelope eventEnvelope) throws IllegalAmqpEventCoordinate {
        def routingKey = routingKey(eventEnvelope)
        try {
            channel.basicPublish EVENT_EXCHANGE_NAME, routingKey, NO_PROPERTIES, eventEnvelope.toJSON().bytes
            log.debug "Published ${eventEnvelope.toJSON()} to '$EVENT_EXCHANGE_NAME' with routingKey '${routingKey}'"
        } catch(Exception e) {
            log.warn "Exception during publishing $eventEnvelope to $EVENT_EXCHANGE_NAME", e
        }
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
