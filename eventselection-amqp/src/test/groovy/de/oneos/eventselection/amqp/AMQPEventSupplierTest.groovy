package de.oneos.eventselection.amqp

import com.rabbitmq.client.*
import de.oneos.eventselection.*
import de.oneos.eventsourcing.*
import de.oneos.eventstore.*
import org.junit.*

import static org.hamcrest.Matchers.*
import static org.junit.Assert.*
import static org.mockito.Mockito.*

class AMQPEventSupplierTest {

    static final UUID NO_CORRELATION_ID = null
    static final String USER_UNKNOWN = null
    static final String APPLICATION_NAME = 'APPLICATION NAME'
    static final String BOUNDED_CONTEXT_NAME = 'BOUNDED CONTEXT NAME'
    static final String AGGREGATE_NAME = 'AGGREGATE NAME'
    static final String EVENT_NAME = 'EVENT NAME'

    Channel channel = mock(Channel)

    EventFilter unconstrainedEventFilter = mock(EventFilter)
    EventPublisher eventPublisher = mock(EventPublisher)

    AMQP.Queue.DeclareOk queueDeclareOk
    Connection connection

    EventPublisher amqpEventPublisher
    EventSupplier amqpEventSupplier

    def generatedAggregateId = UUID.randomUUID()
    def businessEventHappened = new BusinessEventHappened()

    EventEnvelope boxedBusinessEvent = new EventEnvelope(
        APPLICATION_NAME,
        BOUNDED_CONTEXT_NAME,
        AGGREGATE_NAME,
        generatedAggregateId,
        businessEventHappened,
        NO_CORRELATION_ID,
        USER_UNKNOWN
    )


    @Before
    void setUp() {
        queueDeclareOk = new AMQP.Queue.DeclareOk.Builder().queue('GENERATED QUEUE NAME').build()

        when(channel.queueDeclare()).thenReturn(queueDeclareOk)

        when(unconstrainedEventFilter.withConstrainedValues(anyObject(), anyObject())).thenReturn('*.*.*.*')

        def connectionFactory = new ConnectionFactory()
        connectionFactory.clientProperties = AMQPConstants.DEFAULT_AMQP_CLIENT_PROPERTIES
        try {
            connection = connectionFactory.newConnection()
        } catch (ConnectException) {
            fail('Couldn\'t connect to AMQP. Try running `bin/services start`.')
        }

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

        amqpEventSupplier.subscribeTo(unconstrainedEventFilter, eventPublisher)

        verify(channel).queueBind(amqpEventSupplier.queueName, AMQPConstants.EVENT_EXCHANGE_NAME, AMQPEventSupplier.routingKey(unconstrainedEventFilter))
    }

    @Test
    void should_pass_events_to_the_EventProcessor() {
        amqpEventSupplier = new AMQPEventSupplier(connection)
        amqpEventSupplier.subscribeTo(unconstrainedEventFilter, eventPublisher)

        amqpEventPublisher.publish(boxedBusinessEvent)

        // Because AMQP works inherently asynchronously we have to wait
        sleep(100)

        verify(eventPublisher).publish(boxedBusinessEvent)
    }

    static class BusinessEventHappened extends BaseEvent {
        String name = 'Business event happened'
    }
}
