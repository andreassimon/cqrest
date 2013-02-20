package de.oneos.cqrs.readmodels.amqp

import com.rabbitmq.client.AMQP
import com.rabbitmq.client.Channel
import com.rabbitmq.client.Connection
import com.rabbitmq.client.ConnectionFactory
import de.oneos.cqrs.readmodels.EventFilter
import de.oneos.cqrs.readmodels.EventProcessor
import de.oneos.cqrs.readmodels.EventSupplier
import de.oneos.cqrs.readmodels.MapEventFilter
import domain.events.Event
import domain.events.EventEnvelope
import framework.EventPublisher
import org.junit.Before
import org.junit.Test

import static org.hamcrest.Matchers.equalTo
import static org.junit.Assert.assertThat
import static org.mockito.Matchers.anyObject
import static org.mockito.Mockito.*

class AMQPEventSupplierTest {

    static final String APPLICATION_NAME = 'APPLICATION NAME'
    static final String BOUNDED_CONTEXT_NAME = 'BOUNDED CONTEXT NAME'
    static final String AGGREGATE_NAME = 'AGGREGATE NAME'
    static final String EVENT_NAME = 'EVENT NAME'

    Channel channel = mock(Channel)

    EventFilter unconstrainedEventFilter = mock(EventFilter)
    EventProcessor eventProcessor = mock(EventProcessor)

    AMQP.Queue.DeclareOk queueDeclareOk
    Connection connection

    EventPublisher amqpEventPublisher
    EventSupplier amqpEventSupplier

    def generatedAggregateId = UUID.randomUUID()
    def businessEventHappened = new BusinessEventHappened()

    EventEnvelope boxedBusinessEvent = new EventEnvelope(APPLICATION_NAME, BOUNDED_CONTEXT_NAME, AGGREGATE_NAME, generatedAggregateId, businessEventHappened)


    @Before
    void setUp() {
        queueDeclareOk = new AMQP.Queue.DeclareOk.Builder().queue('GENERATED QUEUE NAME').build()

        when(channel.queueDeclare()).thenReturn(queueDeclareOk)

        when(unconstrainedEventFilter.withConstrainedValues(anyObject(), anyObject())).thenReturn('*.*.*.*')

        def connectionFactory = new ConnectionFactory()
        connectionFactory.clientProperties = AMQPConstants.DEFAULT_AMQP_CLIENT_PROPERTIES
        connection = connectionFactory.newConnection()

        amqpEventPublisher = new AMQPEventPublisher(connection)
    }


    @Test
    void should_create_a_new_queue() {
        amqpEventSupplier = new AMQPEventSupplier(channel)

        verify(channel).queueDeclare()
        assertThat amqpEventSupplier.queueName, equalTo(queueDeclareOk.queue)
    }

    @Test
    void should_join_constrained_event_coordinates() {
        EventFilter eventFilter = new MapEventFilter(
            applicationName: APPLICATION_NAME,
            boundedContextName: BOUNDED_CONTEXT_NAME,
            aggregateName: AGGREGATE_NAME,
            eventName: EVENT_NAME
        )

        assertThat AMQPEventSupplier.routingKey(eventFilter), equalTo(joinEventCoordinates(eventFilter, '.'))
    }

    private joinEventCoordinates(EventFilter eventFilter, String separator) {
        ['applicationName', 'boundedContextName', 'aggregateName', 'eventName'].collect { eventFilter[it] }.join(separator)
    }

    @Test
    void should_replace_wildcard_coordinates_with_asterisk() {
        EventFilter eventFilter = new MapEventFilter()

        assertThat AMQPEventSupplier.routingKey(eventFilter), equalTo('*.*.*.*')
    }

    @Test
    void should_create_a_binding_for_each_registered_event_filter() {
        amqpEventSupplier = new AMQPEventSupplier(channel)

        amqpEventSupplier.subscribeTo(unconstrainedEventFilter, eventProcessor)

        verify(channel).queueBind(amqpEventSupplier.queueName, AMQPConstants.EVENT_EXCHANGE_NAME, AMQPEventSupplier.routingKey(unconstrainedEventFilter))
    }

    @Test
    void should_pass_events_to_the_EventProcessor() {
        amqpEventSupplier = new AMQPEventSupplier(connection)
        amqpEventSupplier.subscribeTo(unconstrainedEventFilter, eventProcessor)

        amqpEventPublisher.publish(boxedBusinessEvent)

        // Because AMQP works inherently asynchronously we have to wait
        sleep(50)

        verify(eventProcessor).process(mapMatching(boxedBusinessEvent))
    }

    private mapMatching(EventEnvelope eventEnvelope) {
        new Object() {
            @Override
            String toString() {
                """{\
applicationName == ${eventEnvelope.applicationName} && \
boundedContextName == ${eventEnvelope.boundedContextName} && \
aggregateName == ${eventEnvelope.aggregateName} && \
aggregateId == ${eventEnvelope.aggregateId} && \
eventName == ${eventEnvelope.eventName}\
}"""
            }

            @Override
            boolean equals(Object that) {
                that.applicationName == eventEnvelope.applicationName &&
                that.boundedContextName == eventEnvelope.boundedContextName &&
                that.aggregateName == eventEnvelope.aggregateName &&
                that.aggregateId == eventEnvelope.aggregateId.toString() &&
                that.eventName == eventEnvelope.eventName
            }
        }
    }

    static class BusinessEventHappened extends Event {
        String name = 'Business event happened'

        Object applyTo(aggregate) { return null }
    }
}
