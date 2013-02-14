package de.oneos.cqrs.readmodels

import com.rabbitmq.client.DefaultConsumer
import com.rabbitmq.client.AMQP
import com.rabbitmq.client.Envelope

import static infrastructure.messaging.AMQPConstants.SINGLE_MESSAGE
import groovy.json.JsonSlurper
import org.springframework.jdbc.core.JdbcTemplate
import javax.sql.DataSource
import org.apache.commons.logging.LogFactory
import com.rabbitmq.client.Channel

class DynamicEventHandler {
    private static final log = LogFactory.getLog(this)

    private static class EventConsumer extends DefaultConsumer {
        def slurper = new JsonSlurper()
        DynamicEventHandler eventHandler

        EventConsumer(DynamicEventHandler eventHandler, Channel channel) {
            super(channel)
            this.eventHandler = eventHandler
        }

        void handleDelivery(String consumerTag, Envelope envelope, AMQP.BasicProperties properties, byte[] body) {
            def message = new String(body);

            def jsonMap = slurper.parseText(message)

            try {
                eventHandler.handleEvent(jsonMap)
            } catch (Exception e) {
                log.info "Exception was raised when handling event '${jsonMap.eventName}' ${jsonMap.attributes}", e
            }
            channel.basicAck(envelope.deliveryTag, SINGLE_MESSAGE)
        }

    }

    String eventName
    DataSource dataSource

    Closure handleEvent = { eventAttributes ->
        log.debug "Default handler for ${toString()} was fired"
    }

    DynamicEventHandler() {
        super()
    }

    void setHandleEvent(Closure handleEvent) {
        handleEvent.delegate = this
        handleEvent.resolveStrategy = Closure.DELEGATE_FIRST
        this.handleEvent = handleEvent
    }

    // TODO Um die Reihenfolge zu garantieren, darf es pro *Read Model* nur eine Queue geben,
    //      Nicht eine Queue pro Listening.
    def bindToTopic(def amqpConnection) {
        def channel = amqpConnection.createChannel()
        def declareOk = channel.queueDeclare()
        def queue = declareOk.queue
        channel.queueBind(queue, "EventExchange", eventName)
        channel.basicConsume(queue, createConsumer(channel))
    }

    EventConsumer createConsumer(channel) {
        new EventConsumer(this, channel)
    }

    void execute(String sql, Object... varargs) {
        JdbcTemplate jdbcTemplate = new JdbcTemplate(dataSource)
        jdbcTemplate.update(sql, varargs)
    }

    def update(Object... varargs) {
        return this
    }

    def delete(Object... varargs) {
        return this
    }

    def where(Object... varargs) {

    }

    @Override
    String toString() {
        return "DynamicEventHandler > $eventName".toString()
    }
}
