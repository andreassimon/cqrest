package de.oneos.eventselection.amqp

import com.rabbitmq.client.*

class AMQPConstants {
    static final AMQP.BasicProperties NO_PROPERTIES = null
    static final boolean NOT_DURABLE = false

    static final boolean EXCLUSIVE = true
    static final boolean NOT_EXCLUSIVE = false

    static final boolean AUTO_DELETE = true
    static final boolean NO_AUTO_DELETE = false

    /**
     * Used for channel.queueDelete attributes ifUnused, and ifEmpty
     */
    static final boolean ALWAYS = false

    static final Map<String, Object> NO_ADDITIONAL_ARGUMENTS = null
    static final Map<String, Object> DEFAULT_AMQP_CLIENT_PROPERTIES = [
            host: 'localhost',
            virtualHost: '/',
            port: 5672,
            username: 'guest',
            password: 'guest',
            requestedHeartbeat: 0
    ]

    static final boolean AUTO_ACK = true
    static final boolean NO_AUTO_ACK = false

    static final boolean SINGLE_MESSAGE = false // Passed to 'multiple' argument

    // See http://www.rabbitmq.com/tutorials/amqp-concepts.html
    static final String DEFAULT_EXCHANGE = ''
    static final String DEFAULT_DIRECT_EXCHANGE  = 'amq.direct'
    static final String DEFAULT_FANOUT_EXCHANGE  = 'amq.fanout'
    static final String DEFAULT_TOPIC_EXCHANGE   = 'amq.topic'
    static final String DEFAULT_HEADERS_EXCHANGE = 'amq.match'


    static final String DIRECT_EXCHANGE = 'direct'
    static final String FANOUT_EXCHANGE = 'fanout'
    static final String TOPIC_EXCHANGE  = 'topic'

    static final String EVENT_EXCHANGE_NAME = "EventExchange"
}
