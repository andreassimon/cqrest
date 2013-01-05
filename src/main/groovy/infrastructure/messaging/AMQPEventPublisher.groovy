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
    final Channel channel

    AMQPEventPublisher() {
        def connectionFactory = new ConnectionFactory()
        connectionFactory.clientProperties = DEFAULT_AMQP_CLIENT_PROPERTIES
        connection = connectionFactory.newConnection()
        channel = connection.createChannel()

        channel.queueDeclare(ReadModelBuilder.MESSAGE_QUEUE, NOT_DURABLE, NOT_EXCLUSIVE, NO_AUTO_DELETE, NO_ADDITIONAL_ARGUMENTS);
    }

    @Override
    void publish(Event<?> event) {
        channel.basicPublish DEFAULT_EXCHANGE, ReadModelBuilder.MESSAGE_QUEUE, NO_PROPERTIES, toJSON(event).bytes

        channel.close()
        connection.close()
    }
}
