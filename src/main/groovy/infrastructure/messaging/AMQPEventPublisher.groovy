package infrastructure.messaging

import com.rabbitmq.client.Channel
import com.rabbitmq.client.Connection
import domain.events.Event
import framework.EventPublisher

import static infrastructure.messaging.AMQPConstants.NO_PROPERTIES
import static infrastructure.utilities.GenericEventSerializer.toJSON

class AMQPEventPublisher implements EventPublisher {
    static final String EVENT_EXCHANGE = "EventExchange"

    final Connection connection
    Channel channel

    AMQPEventPublisher(Connection connection) {
        this.connection = connection
        Channel channel = this.connection.createChannel()
        channel.exchangeDeclare(EVENT_EXCHANGE, "topic")
    }

    @Override
    void publish(Event<?> event) {
        channel = connection.createChannel()

        channel.basicPublish "EventExchange", event.name, NO_PROPERTIES, toJSON(event).bytes

        channel.close()
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
