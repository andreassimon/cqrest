package infrastructure.messaging

import com.rabbitmq.client.Channel
import com.rabbitmq.client.Connection
import com.rabbitmq.client.ConnectionFactory
import domain.events.Event
import framework.EventPublisher
import readmodels.ReadModelBuilder

import static infrastructure.messaging.AMQPConstants.*
import static infrastructure.utilities.GenericEventSerializer.toJSON

class AMQPEventPublisher implements EventPublisher {
    final Connection connection
    Channel channel

    AMQPEventPublisher() {
        def connectionFactory = new ConnectionFactory()
        connectionFactory.clientProperties = DEFAULT_AMQP_CLIENT_PROPERTIES
        connection = connectionFactory.newConnection()
    }

    @Override
    void publish(Event<?> event) {
        channel = connection.createChannel()
        channel.basicPublish DEFAULT_EXCHANGE, ReadModelBuilder.MESSAGE_QUEUE, NO_PROPERTIES, toJSON(event).bytes

        channel.close()
    }

    // TODO finalizing lernen!!!
    @Override
    protected void finalize() throws Throwable {
        try {
            connection.close()
        } catch (Exception) {
        } finally {
            super.finalize()
        }
    }
}
