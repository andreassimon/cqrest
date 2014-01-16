package de.oneos.eventselection.amqp

import com.rabbitmq.client.*
import de.oneos.eventsourcing.Correlation
import de.oneos.eventsourcing.EventBus
import org.apache.commons.logging.*

import static AMQP.*


class AMQPEventBus implements EventBus {
    public static Log log = LogFactory.getLog(this)

    protected Connection _connection
    protected Channel channel


    AMQPEventBus(Connection connection) {
        _connection = connection
        channel = connection.createChannel()

        try {
            channel.exchangeDeclare(CORRELATED_EVENT_EXCHANGE, TOPIC_EXCHANGE, DURABLE, NO_AUTO_DELETE, PUBLIC, [:])
        } catch(IOException e) {
            log.info(e.cause.message, e)
            channel = connection.createChannel()
        }
    }


    @Override
    Correlation subscribeCorrelation(Correlation correlation) {
        if(_connection != null) {
            new CorrelatedEventConsumer(_connection.createChannel(), correlation)
        }
        return correlation
    }

    @Override
    void emit(UUID correlation, String eventType) {
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
