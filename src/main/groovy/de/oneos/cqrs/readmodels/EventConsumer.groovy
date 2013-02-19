package de.oneos.cqrs.readmodels

import com.rabbitmq.client.AMQP
import com.rabbitmq.client.Channel
import com.rabbitmq.client.DefaultConsumer
import com.rabbitmq.client.Envelope
import groovy.json.JsonSlurper

import static de.oneos.cqrs.readmodels.amqp.AMQPConstants.SINGLE_MESSAGE

class EventConsumer extends DefaultConsumer {
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
