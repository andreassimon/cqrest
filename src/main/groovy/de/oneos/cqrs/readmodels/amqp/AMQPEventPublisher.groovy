package de.oneos.cqrs.readmodels.amqp

import com.rabbitmq.client.Channel
import com.rabbitmq.client.Connection
import domain.events.EventEnvelope
import framework.EventPublisher
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
    void publish(EventEnvelope eventEnvelope) {
        try {
            channel.basicPublish EVENT_EXCHANGE_NAME, routingKey(eventEnvelope), NO_PROPERTIES, eventEnvelope.toJSON().bytes
            log.debug "Published $eventEnvelope to $EVENT_EXCHANGE_NAME"
        } catch(Exception e) {
            log.warn "Exception during publishing $eventEnvelope to $EVENT_EXCHANGE_NAME", e
        }
    }

    private static routingKey(EventEnvelope eventEnvelope) {
        ['applicationName', 'boundedContextName', 'aggregateName', 'eventName'].collect {
            eventEnvelope[it]
        }.join('.')
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
