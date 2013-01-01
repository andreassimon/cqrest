package infrastructure.messaging

import com.rabbitmq.client.AMQP

class AMQPConstants {
    public static final AMQP.BasicProperties NO_PROPERTIES = null
    public static final boolean NOT_DURABLE = false
    public static final boolean NOT_EXCLUSIVE = false
    public static final boolean NO_AUTO_DELETE = false
    public static final Map<String, Object> NO_ADDITIONAL_ARGUMENTS = null
    public static final Map<String, Serializable> DEFAULT_AMQP_CLIENT_PROPERTIES = [
            host: 'localhost',
            virtualHost: '/',
            port: 5672,
            username: 'guest',
            password: 'guest',
            requestedHeartbeat: 0
    ]
    public static final String EVENT_QUEUE = "event-queue"
    public static final boolean AUTO_ACK = true
}
