package de.oneos.cqrs.readmodels;


import com.rabbitmq.client.Channel
import com.rabbitmq.client.Connection
import com.rabbitmq.client.impl.AMQImpl
import org.junit.Test

import static org.hamcrest.Matchers.equalTo
import static org.junit.Assert.assertThat
import static org.mockito.Mockito.mock
import static org.mockito.Mockito.when

import com.rabbitmq.client.Envelope
import domain.events.EventEnvelope
import domain.events.Event

public class DynamicEventHandlerTest {
    def deserializedEventEnvelope
    Connection amqpConnection
    Channel amqpChannel
    AMQImpl.Queue.DeclareOk declareOk = new AMQImpl.Queue.DeclareOk('queue-name', 0, 0)
    Envelope amqpEnvelope
    def amqpProperties
    long randomDeliveryTag


    @Test
    public void should_invoke_the_passed_closure() throws Exception {
        amqpConnection = mock(Connection)
        amqpChannel = mock(Channel)
        when(amqpConnection.createChannel()).thenReturn(amqpChannel)
        when(amqpChannel.queueDeclare()).thenReturn(declareOk)

        DynamicEventHandler eventHandler = new DynamicEventHandler()
        eventHandler.setEventName('EVENT NAME');
        eventHandler.setHandleEvent({ eventEnvelope ->
            deserializedEventEnvelope = eventEnvelope
        })
        eventHandler.bindToTopic(amqpConnection)

        def amqpConsumer = eventHandler.createConsumer(amqpChannel)
        randomDeliveryTag = Math.round(Math.random())
        amqpEnvelope = new Envelope(randomDeliveryTag, false, 'EXCHANGE', 'ROUTING KEY')

        def serializedEventEnvelope = new EventEnvelope('APPLICATION NAME', 'BOUNDED CONTEXT NAME', 'AGGREGATE NAME', UUID.randomUUID(), new Event() {
            @Override
            Object applyTo(Object t) {
                return null
            }
        }).toJSON().bytes

        amqpConsumer.handleDelivery('CONSUMER TAG', amqpEnvelope, amqpProperties, serializedEventEnvelope)

        assertThat deserializedEventEnvelope.applicationName, equalTo('APPLICATION NAME')
    }
}
