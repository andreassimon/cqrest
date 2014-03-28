package de.oneos.eventselection.amqp

import com.rabbitmq.client.Channel
import com.rabbitmq.client.Connection

import org.apache.commons.logging.Log
import org.apache.commons.logging.LogFactory

import de.oneos.eventsourcing.EventConsumer
import de.oneos.eventsourcing.EventEnvelope
import de.oneos.eventsourcing.EventStream
import de.oneos.eventsourcing.EventSupplier
import de.oneos.eventsourcing.ObservableEventSupplier

import static AMQP.*


class AMQPEventPublisher implements EventConsumer, org.cqrest.reactive.Observer<EventEnvelope> {
    static Log log = LogFactory.getLog(AMQPEventPublisher)

    Channel channel

    AMQPEventPublisher(Connection connection, EventSupplier upstream) {
        channel = connection.createChannel()
        try {
            channel.exchangeDeclare(EVENT_EXCHANGE_NAME, TOPIC_EXCHANGE, DURABLE, NO_AUTO_DELETE, PUBLIC, [:])
        } catch(IOException e) {
            log.info(e.cause.message, e)
            channel = connection.createChannel()
        }
        channel.exchangeDeclare(EVENT_QUERY_EXCHANGE_NAME, DIRECT_EXCHANGE)
        setEventSource(new ObservableEventSupplier(upstream))
    }


    @Override
    void onCompleted() {
        // TODO implement
        log.warn "AMQPEventPublisher#onCompleted is not implemented properly! Do it! NOW!"
        log.debug "[$this] Event stream was completed"
    }

    @Override
    void onError(Throwable e) {
        // TODO implement
        log.warn "AMQPEventPublisher#onError is not implemented properly! Do it! NOW!"
        log.warn "[$this] ${e.getClass().getCanonicalName()} was raised when processing event stream", e
    }

    @Override
    void onNext(EventEnvelope args) {
        process(args)
    }

    @Override
    @Deprecated
    // TODO Inline
    void process(EventEnvelope eventEnvelope) throws IllegalAmqpEventCoordinate {
        def routingKey = routingKey(eventEnvelope)
        try {
            channel.basicPublish EVENT_EXCHANGE_NAME, routingKey, NO_PROPERTIES, eventEnvelope.toJSON().bytes
            if(log.isDebugEnabled()) {
                log.debug "Published ${eventEnvelope.toJSON()} to '$EVENT_EXCHANGE_NAME' with routingKey '${routingKey}'"
            }
        } catch(Exception e) {
            log.warn "Exception during publishing $eventEnvelope to $EVENT_EXCHANGE_NAME", e
        }
    }

    @Override
    @Deprecated
    // TODO Remove
    void wasRegisteredAt(EventSupplier eventSupplier) {
        assert eventSupplier != null

        com.rabbitmq.client.AMQP.Queue.DeclareOk declareOk = channel.queueDeclare()
        channel.queueBind(declareOk.queue, EVENT_QUERY_EXCHANGE_NAME, EVENT_QUERY)
        channel.basicConsume(declareOk.queue, new EventQueryConsumer(channel, eventSupplier))
        log.debug("Bound event query queue '$declareOk.queue' to event supplier '$eventSupplier'")

        try {
            eventSupplier.subscribeTo([:], this)
        } catch(e) {
            log.warn("${e.getClass().getCanonicalName()} was thrown when subscribing $this to $eventSupplier", e)
        }
    }

    void setEventSource(EventStream eventSource) {
        assert eventSource

        com.rabbitmq.client.AMQP.Queue.DeclareOk declareOk = channel.queueDeclare()
        channel.queueBind(declareOk.queue, EVENT_QUERY_EXCHANGE_NAME, EVENT_QUERY)
        channel.basicConsume(declareOk.queue, new EventQueryConsumer(channel, eventSource))
        log.debug("Bound event query queue '$declareOk.queue' to event source '$eventSource'")

        try {
            eventSource.observe([:]).subscribe(this)
        } catch(e) {
            log.warn("${e.getClass().getCanonicalName()} was thrown when subscribing $this to $eventSource", e)
        }
    }

    private static routingKey(EventEnvelope eventEnvelope) throws IllegalAmqpEventCoordinate {
        def eventCoordinates = ['applicationName', 'boundedContextName', 'aggregateName', 'eventName'].collect {
            eventEnvelope[it] as String
        }
        if(eventCoordinates.any { it.contains('.') }) {
            throw new IllegalAmqpEventCoordinate(eventCoordinates)
        }
        eventCoordinates.join('.')
    }

    // TODO finalizing lernen!!!
    @Override
    void finalize() throws Throwable {
        try {
            channel.close()
        } catch (Exception) {
        } finally {
            super.finalize()
        }
    }
}
