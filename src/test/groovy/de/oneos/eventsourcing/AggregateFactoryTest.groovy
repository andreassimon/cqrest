package de.oneos.eventsourcing

import static java.util.UUID.randomUUID

import org.junit.Test
import static org.junit.Assert.*
import static org.hamcrest.Matchers.*
import static org.mockito.Mockito.*

import de.oneos.eventstore.EventAggregator
import org.junit.Before


class AggregateFactoryTest {
    AggregateFactory aggregateFactory
    UUID AGGREGATE_ID = randomUUID()

    EventAggregator eventAggregator

    @Before
    void setUp() {
        eventAggregator = mock(EventAggregator)
    }


    @Test
    void should_dynamically_add_method__emit__to_created_instances() {
        aggregateFactory = new AggregateFactory()

        Aggregate aggregate = aggregateFactory.newInstance(Aggregate, AGGREGATE_ID)

        assertThat aggregate.respondsTo('emit', Event), not(empty())
    }

    @Test
    void emit__should_pass_the_event_to_EventAggregator() {
        aggregateFactory = new AggregateFactory()
        Aggregate aggregate = aggregateFactory.newInstance(Aggregate, AGGREGATE_ID)
        aggregate.eventAggregator = eventAggregator

        2.times {
            aggregate.someBusinessMethod()
        }

        verify(eventAggregator, times(2)).publishEvent(
            Aggregate.applicationName,
            Aggregate.boundedContextName,
            Aggregate.aggregateName,
            AGGREGATE_ID,
            new Business_event_happened()
        )
    }



    static class Aggregate {
        static applicationName = 'APPLICATION'
        static boundedContextName = 'BOUNDED CONTEXT'
        static aggregateName = 'AGGREGATE'

        void someBusinessMethod() {
            emit(new Business_event_happened())
        }
    }

    static class Business_event_happened extends Event {
        @Override
        def applyTo(aggregate) { aggregate }
    }

}
