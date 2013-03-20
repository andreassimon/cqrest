package de.oneos.eventsourcing

import static java.util.UUID.randomUUID

import org.junit.Test
import static org.junit.Assert.*
import static org.hamcrest.Matchers.*
import static org.mockito.Mockito.*

import org.junit.Before


class DefaultAggregateFactoryTest {
    DefaultAggregateFactory aggregateFactory
    UUID AGGREGATE_ID = randomUUID()
    UUID ANOTHER_AGGREGATE_ID = randomUUID()

    Aggregate aggregate
    List<Aggregate> aggregates
    EventAggregator eventAggregator

    @Before
    void setUp() {
        aggregateFactory = new DefaultAggregateFactory()

        while(AGGREGATE_ID == ANOTHER_AGGREGATE_ID) {
            ANOTHER_AGGREGATE_ID = randomUUID()
        }

        eventAggregator = mock(EventAggregator)
    }


    @Test(expected = IllegalArgumentException)
    void should_throw_IllegalArgumentException_when_the_rawAggregateClass_does_not_have_attribute_applicationName() {
        aggregate = aggregateFactory.newInstance(
            RawAggregateClass_without_applicationName,
            AGGREGATE_ID,
            eventAggregator,
            listOfEvents()
        )
    }

    static class RawAggregateClass_without_applicationName {
        // static final applicationName = 'APPLICATION'
        static final boundedContextName = 'BOUNDED CONTEXT'
        static final aggregateName = 'AGGREGATE'
    }

    @Test(expected = IllegalArgumentException)
    void should_throw_IllegalArgumentException_when_the_rawAggregateClass_does_not_have_attribute_boundedContextName() {
        aggregateFactory.newInstance(
            RawAggregateClass_without_boundedContextName,
            AGGREGATE_ID,
            eventAggregator,
            listOfEvents()
        )
    }

    static class RawAggregateClass_without_boundedContextName {
        static final applicationName = 'APPLICATION'
        // static final boundedContextName = 'BOUNDED CONTEXT'
        static final aggregateName = 'AGGREGATE'
    }

    @Test(expected = IllegalArgumentException)
    void should_throw_IllegalArgumentException_when_the_rawAggregateClass_does_not_have_attribute_aggregateName() {
        aggregateFactory.newInstance(
            RawAggregateClass_without_aggregateName,
            AGGREGATE_ID,
            eventAggregator,
            listOfEvents()
        )
    }

    static class RawAggregateClass_without_aggregateName {
        static final applicationName = 'APPLICATION'
        static final boundedContextName = 'BOUNDED CONTEXT'
        // static final aggregateName = 'AGGREGATE'
    }

    @Test(expected = EventNotApplicable)
    void should_throw_EventNotApplicable_when_stored_events_can_not_be_applied_to_the_aggregate_class() {
        aggregateFactory.newInstance(
            IncompatibleAggregateClass,
            AGGREGATE_ID,
            eventAggregator,
            listOfEvents()
        )
    }

    static class IncompatibleAggregateClass {
        static final applicationName = 'APPLICATION'
        static final boundedContextName = 'BOUNDED CONTEXT'
        static final aggregateName = 'AGGREGATE'

        final UUID id

        IncompatibleAggregateClass(UUID id) {
            this.id = id
        }
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



    static class Aggregate {
        static applicationName = 'APPLICATION'
        static boundedContextName = 'BOUNDED CONTEXT'
        static aggregateName = 'AGGREGATE'

        final UUID id
        boolean businessEventWasApplied = false
        int numberOfAppliedEvents = 0

        Aggregate(UUID id) {
            this.id = id
        }

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
