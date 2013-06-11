package de.oneos.eventselection.amqp

import com.rabbitmq.client.*

class AMQPConstants {
    public static final boolean DURABLE = true
    public static final boolean NOT_DURABLE = !DURABLE

    public static final boolean EXCLUSIVE = true
    public static final boolean NOT_EXCLUSIVE = !EXCLUSIVE

    public static final boolean AUTO_DELETE = true
    public static final boolean NO_AUTO_DELETE = !AUTO_DELETE

    public static final boolean INTERNAL = true
    public static final boolean PUBLIC = !INTERNAL

    /**
     * Used for channel.queueDelete attributes ifUnused, and ifEmpty
     */
    public static final boolean ALWAYS = false

    public static final Map<String, Object> NO_ADDITIONAL_ARGUMENTS = null
    public static final Map<String, Object> DEFAULT_AMQP_CLIENT_PROPERTIES = [
            host: 'localhost',
            virtualHost: '/',
            port: 5672,
            username: 'guest',
            password: 'guest',
            requestedHeartbeat: 0
    ]

    public static final boolean AUTO_ACK = true
    public static final boolean NO_AUTO_ACK = false

    public static final boolean SINGLE_MESSAGE = false // Passed to 'multiple' argument

    // See http://www.rabbitmq.com/tutorials/amqp-concepts.html
    public static final String DEFAULT_EXCHANGE = ''
    public static final String DEFAULT_DIRECT_EXCHANGE  = 'amq.direct'
    public static final String DEFAULT_FANOUT_EXCHANGE  = 'amq.fanout'
    public static final String DEFAULT_TOPIC_EXCHANGE   = 'amq.topic'
    public static final String DEFAULT_HEADERS_EXCHANGE = 'amq.match'


    public static final String DIRECT_EXCHANGE = 'direct'
    public static final String FANOUT_EXCHANGE = 'fanout'
    public static final String TOPIC_EXCHANGE  = 'topic'

    public static final String EVENT_EXCHANGE_NAME = "EventExchange"
    public static final String EVENT_QUERY_EXCHANGE_NAME = "EventQueryExchange"
    public static final String EVENT_QUERY = "event-query"


    public static final BasicProperties NO_PROPERTIES = null

    public static BasicProperties replyTo(String queueName) {
        new AMQP.BasicProperties().builder().replyTo(queueName).build()
    }

}
