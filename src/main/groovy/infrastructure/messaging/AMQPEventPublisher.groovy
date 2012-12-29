package infrastructure.messaging

import domain.events.Event
import framework.EventPublisher
import com.rabbitmq.client.*
import infrastructure.utilities.GenericEventSerializer

import static infrastructure.utilities.GenericEventSerializer.toJSON

class AMQPEventPublisher implements EventPublisher {
    public static final AMQP.BasicProperties NO_PROPERTIES = null
    public static final Map<String, Serializable> DEFAULT_AMQP_CLIENT_PROPERTIES = [
            host: 'localhost',
            virtualHost: '/',
            port: 5672,
            username: 'guest',
            password: 'guest',
            requestedHeartbeat: 0
    ]
    public static final boolean NOT_DURABLE = false
    public static final boolean NOT_EXCLUSIVE = false
    public static final boolean NO_AUTO_DELETE = false
    public static final Map<String, Object> NO_ADDITIONAL_ARGUMENTS = null
    def connectionFactory = new ConnectionFactory()
    public static final String QUEUE_NAME = "event-queue"

    AMQPEventPublisher() {
        connectionFactory.clientProperties = DEFAULT_AMQP_CLIENT_PROPERTIES
    }

    @Override
    void publish(Event<?> event) {
        def connection = connectionFactory.newConnection()
        def channel = connection.createChannel()

        channel.queueDeclare(QUEUE_NAME, NOT_DURABLE, NOT_EXCLUSIVE, NO_AUTO_DELETE, NO_ADDITIONAL_ARGUMENTS);

        channel.basicPublish '', QUEUE_NAME, NO_PROPERTIES, toJSON(event).bytes

        channel.close()
        connection.close()
    }
}
