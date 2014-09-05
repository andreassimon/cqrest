package de.oneos.eventselection.amqp

import org.junit.*
import static org.junit.Assert.*
import static org.hamcrest.Matchers.*
import static org.mockito.Mockito.*

import com.rabbitmq.client.*

import de.oneos.eventsourcing.BaseEvent
import de.oneos.eventsourcing.EventConsumer
import de.oneos.eventsourcing.EventEnvelope
import de.oneos.eventsourcing.EventSupplier


class AMQPEventSupplierTest {

    static final UUID NO_CORRELATION_ID = null
    static final String USER_UNKNOWN = null
    static final String APPLICATION_NAME = 'APPLICATION NAME'
    static final String BOUNDED_CONTEXT_NAME = 'BOUNDED CONTEXT NAME'
    static final String AGGREGATE_NAME = 'AGGREGATE NAME'
    static final String EVENT_NAME = 'EVENT NAME'

    Channel channel = mock(Channel)

    Map<String, ?> unconstrainedCriteria = [:]
    EventConsumer eventConsumer = mock(EventConsumer)

    com.rabbitmq.client.AMQP.Queue.DeclareOk queueDeclareOk
    Connection connection

    EventSupplier upstreamEventSupplier = new StubEventSupplier()
    AMQPEventPublisher amqpEventPublisher
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
        queueDeclareOk = new com.rabbitmq.client.AMQP.Queue.DeclareOk.Builder().queue('GENERATED QUEUE NAME').build()

        when(channel.queueDeclare()).thenReturn(queueDeclareOk)

        def connectionFactory = new ConnectionFactory()
        connectionFactory.clientProperties = AMQP.DEFAULT_AMQP_CLIENT_PROPERTIES
        connectionFactory.virtualHost = 'cqrs-test'
        try {
            connection = connectionFactory.newConnection()
        } catch (ConnectException) {
            fail('Couldn\'t connect to AMQP. Try running `bin/services start`.')
        }

        amqpEventPublisher = new AMQPEventPublisher(connection, upstreamEventSupplier)
    }


    @Test
    void should_join_constrained_event_coordinates() {
        Map<String, ?> criteria = [
            applicationName: APPLICATION_NAME,
            boundedContextName: BOUNDED_CONTEXT_NAME,
            aggregateName: AGGREGATE_NAME,
            eventName: EVENT_NAME
        ]

        assertThat AMQPEventSupplier.routingKey(criteria), equalTo(joinEventCoordinates(criteria, '.'))
    }

    private joinEventCoordinates(Map<String, ?> criteria, String separator) {
        ['applicationName', 'boundedContextName', 'aggregateName', 'eventName'].collect { criteria[it] }.join(separator)
    }

    @Test
    void should_replace_wildcard_coordinates_with_asterisk() {
        Map<String, ?> criteria = [:]

        assertThat AMQPEventSupplier.routingKey(criteria), equalTo('*.*.*.*')
    }

    @Test
    void should_pass_events_to_the_EventConsumer() {
        amqpEventSupplier = new AMQPEventSupplier(connection, amqpEventPublisher)
        amqpEventSupplier.subscribeTo(unconstrainedCriteria, eventConsumer)

        amqpEventPublisher.process(boxedBusinessEvent)

        // Because AMQP works inherently asynchronously we have to wait
        sleep(100)

        verify(eventConsumer).process(boxedBusinessEvent)
    }

    static class BusinessEventHappened extends BaseEvent {
        String name = 'Business event happened'
    }
}
