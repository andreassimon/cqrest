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
    UUID ANOTHER_AGGREGATE_ID = randomUUID()

    EventAggregator eventAggregator

    @Before
    void setUp() {
        while(AGGREGATE_ID == ANOTHER_AGGREGATE_ID) {
            ANOTHER_AGGREGATE_ID = randomUUID()
        }

        eventAggregator = mock(EventAggregator)
    }


    @Test
    void should_dynamically_add_method__emit__to_created_instances() {
        aggregateFactory = new AggregateFactory()

        Aggregate aggregate = aggregateFactory.newInstance(Aggregate, aggregateId: AGGREGATE_ID)

        assertThat aggregate.respondsTo('emit', Event), not(empty())
    }

    @Test
    void emit__should_pass_the_event_to_EventAggregator() {
        aggregateFactory = new AggregateFactory()
        Aggregate aggregate = aggregateFactory.newInstance(Aggregate,
            aggregateId: AGGREGATE_ID,
            eventAggregator: eventAggregator
        )
        Aggregate anotherAggregate = aggregateFactory.newInstance(Aggregate,
            aggregateId: ANOTHER_AGGREGATE_ID,
            eventAggregator: eventAggregator
        )

        2.times {
            aggregate.emit__Business_event_happened()
            anotherAggregate.emit__Business_event_happened()
        }

        verify(eventAggregator, times(2)).publishEvent(
            Aggregate.applicationName,
            Aggregate.boundedContextName,
            Aggregate.aggregateName,
            AGGREGATE_ID,
            new Business_event_happened()
        )
        verify(eventAggregator, times(2)).publishEvent(
            Aggregate.applicationName,
            Aggregate.boundedContextName,
            Aggregate.aggregateName,
            ANOTHER_AGGREGATE_ID,
            new Business_event_happened()
        )
    }



    static class Aggregate {
        static applicationName = 'APPLICATION'
        static boundedContextName = 'BOUNDED CONTEXT'
        static aggregateName = 'AGGREGATE'

        void emit__Business_event_happened() {
            emit(new Business_event_happened())
        }
    }

    static class Business_event_happened extends Event {
        @Override
        def applyTo(aggregate) { aggregate }
    }

}
