package de.oneos.eventselection.amqp

import org.junit.*
import static org.mockito.Mockito.*

import com.rabbitmq.client.*

import de.oneos.eventstore.*


class AMQPEventTransportTest {
    static final String APPLICATION = 'APPLICATION'
    static final String BOUNDED_CONTEXT = 'BOUNDED CONTEXT'
    static final String AGGREGATE = 'AGGREGATE'
    static final String USER = 'USER'

    static final UUID AGGREGATE_ID = UUID.fromString('8a4e8dec-1565-43ca-88d5-fd31541d7041')
    static final UUID CORRELATION_ID = UUID.fromString('d158a594-c864-4f36-ab34-238de28e8015')

    final thiz = this

    EventProcessor eventPublisher
    EventProcessor eventProcessor
    AMQPEventSupplier eventSupplier

    EventEnvelope publishedEventEnvelope


    @Before
    void setUp() {
        ConnectionFactory connectionFactory = new ConnectionFactory();
        connectionFactory.virtualHost = 'one-os-test'
        Connection connection = connectionFactory.newConnection()

        eventPublisher = new AMQPEventPublisher(connection)

        eventProcessor = mock(EventProcessor)
        eventSupplier = new AMQPEventSupplier(connection)

        publishedEventEnvelope = new EventEnvelope(APPLICATION, BOUNDED_CONTEXT, AGGREGATE, AGGREGATE_ID, [ eventName: 'My event', eventAttributes: [attribute: 'value'] ], 0, new Date(2013 - 1900, 5, 11, 12, 00), CORRELATION_ID, USER)
    }

    @Test
    void should_notify_registration_to_EventProcessor() {
        eventSupplier.eventProcessors = [eventProcessor]

        verify(eventProcessor).wasRegisteredAt(eventSupplier)
    }

    @Test(timeout = 2000L)
    void should_deliver_published_EventEnvelopes_to_registered_EventProcessors() {
        eventSupplier.eventProcessors = [synchronize(eventProcessor)]

        publish(publishedEventEnvelope)

        verify(eventProcessor).process(publishedEventEnvelope)
    }

    protected EventProcessor synchronize(EventProcessor targetEventProcessor) {
        return [
            wasRegisteredAt: {},
            process: { EventEnvelope eventEnvelope ->
                synchronized(thiz) {
                    thiz.notifyAll();
                }
                targetEventProcessor.process(eventEnvelope)
            }
        ] as EventProcessor
    }

    protected void publish(EventEnvelope eventEnvelope) {
        eventPublisher.process(eventEnvelope)
        synchronized(thiz) {
            thiz.wait(3000);
        }
    }

}

