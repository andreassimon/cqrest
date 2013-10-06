package de.oneos.eventsourcing

import org.apache.commons.logging.*
import com.rabbitmq.client.*

import static de.oneos.AMQP.*


class EventBus {
    static Log log = LogFactory.getLog(EventBus)

    protected static Connection _connection
    protected static Channel channel

    static void setConnection(Connection connection) {
        _connection = connection
        channel = connection.createChannel()

        try {
            channel.exchangeDeclare(CORRELATED_EVENT_EXCHANGE, TOPIC_EXCHANGE, DURABLE, NO_AUTO_DELETE, PUBLIC, [:])
        } catch(IOException e) {
            log.info(e.cause.message, e)
            channel = connection.createChannel()
        }
    }

    static Correlation subscribeCorrelation(Correlation correlation) {
        // TODO Extract AMQP communication as a Strategy for testing purposes
        if(_connection != null) {
            new CorrelatedEventConsumer(_connection.createChannel(), correlation)
        }
        return correlation
    }

    static void emit(UUID correlation, String eventType) {
        assert correlation != null
        assert eventType != null

        if(channel == null) {
            log.warn "Cannot emit '$eventType' correlated with {$correlation}. AMQP channel is not initialized."
        } else {
            log.debug "Emitting '$eventType' correlated with {$correlation}"
            try {
                channel.basicPublish(CORRELATED_EVENT_EXCHANGE, correlation.toString(), NO_PROPERTIES, eventType.bytes)
            } catch(Exception e) {
                log.warn "Exception during emitting '$eventType' correlated with {$correlation}", e
            }
        }
    }

}
