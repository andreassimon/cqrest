package de.oneos.cqrs.readmodels

import org.apache.commons.logging.LogFactory
import org.springframework.jdbc.core.JdbcTemplate

import javax.sql.DataSource

class DynamicEventHandler {
    private static final log = LogFactory.getLog(this)

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
