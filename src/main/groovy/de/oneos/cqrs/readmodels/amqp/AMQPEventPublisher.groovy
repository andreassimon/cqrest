package de.oneos.cqrs.readmodels.amqp

import com.rabbitmq.client.Channel
import com.rabbitmq.client.Connection
import domain.events.EventEnvelope
import framework.EventPublisher

import static de.oneos.cqrs.readmodels.amqp.AMQPConstants.*

class AMQPEventPublisher implements EventPublisher {
    final Connection connection
    Channel channel

    AMQPEventPublisher(Connection connection) {
        this.connection = connection
        Channel channel = this.connection.createChannel()
        channel.exchangeDeclare(EVENT_EXCHANGE_NAME, TOPIC_EXCHANGE)
    }

    @Override
    void publish(EventEnvelope eventEnvelope) {
        channel = connection.createChannel()

        channel.basicPublish "EventExchange", routingKey(eventEnvelope), de.oneos.cqrs.readmodels.amqp.AMQPConstants.NO_PROPERTIES, eventEnvelope.toJSON().bytes

        channel.close()
    }

    private routingKey(EventEnvelope eventEnvelope) {
        ['applicationName', 'boundedContextName', 'aggregateName', 'eventName'].collect {
            eventEnvelope[it]
        }.join('.')
    }

    // TODO finalizing lernen!!!
    @Override
    void finalize() throws Throwable {
        try {
            connection.close()
        } catch (Exception) {
        } finally {
            super.finalize()
        }
    }
}
