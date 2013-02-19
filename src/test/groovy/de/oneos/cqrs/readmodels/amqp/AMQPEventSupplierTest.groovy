package de.oneos.cqrs.readmodels.amqp

import com.rabbitmq.client.AMQP
import com.rabbitmq.client.Channel
import de.oneos.cqrs.readmodels.EventFilter
import de.oneos.cqrs.readmodels.EventProcessor
import de.oneos.cqrs.readmodels.EventSupplier
import de.oneos.cqrs.readmodels.MapEventFilter
import org.junit.Before
import org.junit.Ignore
import org.junit.Test

import static org.hamcrest.Matchers.equalTo
import static org.junit.Assert.assertThat
import static org.mockito.Mockito.*

class AMQPEventSupplierTest {

    Channel channel = mock(Channel)

    EventFilter eventFilter = mock(EventFilter)
    EventProcessor eventProcessor = mock(EventProcessor)

    AMQP.Queue.DeclareOk queueDeclareOk

    @Before
    void setUp() {
        queueDeclareOk = new AMQP.Queue.DeclareOk.Builder().queue('GENERATED QUEUE NAME').build()

        when(channel.queueDeclare()).thenReturn(queueDeclareOk)
    }


    @Test
    void should_create_a_new_queue() {
        EventSupplier amqpEventSupplier = new AMQPEventSupplier(channel: channel)

        verify(channel).queueDeclare()
        assertThat amqpEventSupplier.queueName, equalTo(queueDeclareOk.queue)
    }

    @Test
    void should_join_constrained_event_coordinates() {
        EventFilter eventFilter = new MapEventFilter(
            applicationName: 'APPLICATION NAME',
            boundedContextName: 'BOUNDED CONTEXT NAME',
            aggregateName: 'AGGREGATE NAME',
            eventName: 'EVENT NAME'
        )
        assertThat AMQPEventSupplier.routingKey(eventFilter), equalTo("${eventFilter.applicationName}.${eventFilter.boundedContextName}.${eventFilter.aggregateName}.${eventFilter.eventName}".toString())
    }

    @Test
    void should_replace_wildcard_coordinates_with_asterisk() {
        EventFilter eventFilter = new MapEventFilter()

        assertThat AMQPEventSupplier.routingKey(eventFilter), equalTo('*.*.*.*')
    }

    @Test
    void should_create_a_binding_for_each_registered_event_filter() {
        EventSupplier amqpEventSupplier = new AMQPEventSupplier(channel: channel)

        amqpEventSupplier.subscribeTo(eventFilter, eventProcessor)

        verify(channel).queueBind(amqpEventSupplier.queueName, AMQPConstants.EVENT_EXCHANGE_NAME, AMQPEventSupplier.routingKey(eventFilter))
    }

    @Ignore
    @Test
    void should_pass_events_to_the_EventProcessor() {

    }
}
