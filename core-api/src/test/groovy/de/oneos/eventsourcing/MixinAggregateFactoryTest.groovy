package de.oneos.eventsourcing

import static java.util.UUID.randomUUID

import org.junit.Test
import static org.junit.Assert.*
import static org.hamcrest.Matchers.*
import static org.mockito.Mockito.*

import org.junit.Before


class MixinAggregateFactoryTest {
    MixinAggregateFactory aggregateFactory
    UUID AGGREGATE_ID = randomUUID()
    UUID ANOTHER_AGGREGATE_ID = randomUUID()

    Aggregate aggregate
    List<Aggregate> aggregates
    EventAggregator eventAggregator

    @Before
    void setUp() {
        aggregateFactory = new MixinAggregateFactory()

        while(AGGREGATE_ID == ANOTHER_AGGREGATE_ID) {
            ANOTHER_AGGREGATE_ID = randomUUID()
        }

        eventAggregator = mock(EventAggregator)
    }


    @Test
    void should_apply_events_from_the_EventStore_to_loaded_aggregates() {
        aggregate = aggregateFactory.newInstance(
            Aggregate,
            AGGREGATE_ID,
            eventAggregator,
            listOfEvents(function: { Aggregate aggregate -> aggregate.numberOfAppliedEvents++ })
        )

        assertThat aggregate.numberOfAppliedEvents, equalTo(listOfEvents().size())
    }

    List<Event> listOfEvents(eventProperties = [:]) {
        (0..2).collect {
            new Business_event_happened(eventProperties)
        }
    }

    @Test
    void should_dynamically_add_method__emit__to_created_instances() {
        aggregate = aggregateFactory.newInstance(Aggregate, AGGREGATE_ID, eventAggregator, [])

        assertThat aggregate.respondsTo('emit', Event), not(empty())
    }

    @Test
    void emit__should_pass_the_event_to_EventAggregator() {
        aggregates = [AGGREGATE_ID, ANOTHER_AGGREGATE_ID].collect { aggregateId ->
            aggregateFactory.newInstance(Aggregate,
                aggregateId,
                eventAggregator,
                []
            )
        }

        2.times {
            aggregates.each {
                it.emit__Business_event_happened()
            }
        }

        [AGGREGATE_ID, ANOTHER_AGGREGATE_ID].each { aggregateId ->
            verify(eventAggregator, times(2)).publishEvent(
                Aggregate.applicationName,
                Aggregate.boundedContextName,
                Aggregate.aggregateName,
                aggregateId,
                new Business_event_happened()
            )
        }
    }

    @Test
    void emit__should_apply_the_event_to_the_emitting_aggregate_immediately() {
        aggregate = aggregateFactory.newInstance(Aggregate,
            AGGREGATE_ID,
            eventAggregator,
            []
        )

        aggregate.emit__Business_event_happened()

        assertThat aggregate.businessEventWasApplied, equalTo(true)
    }



    static class Aggregate {
        static applicationName = 'APPLICATION'
        static boundedContextName = 'BOUNDED CONTEXT'
        static aggregateName = 'AGGREGATE'

        boolean businessEventWasApplied = false
        int numberOfAppliedEvents = 0

        void emit__Business_event_happened() {
            emit(new Business_event_happened())
        }
    }

    static class Business_event_happened extends Event<Aggregate> {
        static { UNSERIALIZED_PROPERTIES << 'function' }
        Closure<Void> function = { it.businessEventWasApplied = true }

        @Override
        void applyTo(Aggregate aggregate) {
            function(aggregate)
        }
    }

}
