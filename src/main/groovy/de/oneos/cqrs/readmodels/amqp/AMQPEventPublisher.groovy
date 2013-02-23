package de.oneos.cqrs.readmodels.amqp

import com.rabbitmq.client.Channel
import com.rabbitmq.client.Connection
import de.oneos.eventsourcing.EventEnvelope
import de.oneos.eventsourcing.EventPublisher
import org.apache.commons.logging.Log
import org.apache.commons.logging.LogFactory

import static de.oneos.cqrs.readmodels.amqp.AMQPConstants.*

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
