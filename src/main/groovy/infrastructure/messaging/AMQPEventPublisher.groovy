package infrastructure.messaging

import com.rabbitmq.client.ConnectionFactory
import domain.events.Event
import framework.EventPublisher

import static infrastructure.messaging.AMQPConstants.*
import static infrastructure.utilities.GenericEventSerializer.toJSON

class AMQPEventPublisher implements EventPublisher {
    def connectionFactory = new ConnectionFactory()

    AMQPEventPublisher() {
        connectionFactory.clientProperties = DEFAULT_AMQP_CLIENT_PROPERTIES
    }

    @Override
    void publish(Event<?> event) {
        def connection = connectionFactory.newConnection()
        def channel = connection.createChannel()

        channel.queueDeclare(EVENT_QUEUE, NOT_DURABLE, NOT_EXCLUSIVE, NO_AUTO_DELETE, NO_ADDITIONAL_ARGUMENTS);

        channel.basicPublish '', EVENT_QUEUE, NO_PROPERTIES, toJSON(event).bytes

        channel.close()
        connection.close()
    }
}
