package de.oneos.eventsourcing

import static java.util.UUID.randomUUID

import org.junit.Test
import static org.junit.Assert.*
import static org.hamcrest.Matchers.*

import org.junit.Before


class DefaultAggregateFactoryTest {
    DefaultAggregateFactory aggregateFactory
    UUID AGGREGATE_ID = UUID.fromString("62156002-2b9c-4f25-85a7-286f69a8e273")

    Aggregate aggregate

    @Before
    void setUp() {
        aggregateFactory = new DefaultAggregateFactory()
    }

    @Test(expected = IllegalArgumentException)
    void should_throw_IllegalArgumentException_when_the_rawAggregateClass_does_not_have_attribute_aggregateName() {
        aggregateFactory.newInstance(
            RawAggregateClass_without_aggregateName,
            AGGREGATE_ID,
            aggregateHistory
        )
    }

    static class RawAggregateClass_without_aggregateName {
        // static final aggregateName = 'AGGREGATE'
    }

    @Test(expected = EventNotApplicable)
    void should_throw_EventNotApplicable_when_stored_events_can_not_be_applied_to_the_aggregate_class() {
        aggregateFactory.newInstance(
            IncompatibleAggregateClass,
            AGGREGATE_ID,
            aggregateHistory
        )
    }

    static class IncompatibleAggregateClass {
        static final aggregateName = 'Incompatible aggregate'

        final UUID id

        IncompatibleAggregateClass(UUID id) {
            this.id = id
        }
    }

    @Test
    void should_apply_events_from_history_to_loaded_aggregates() {
        aggregate = aggregateFactory.newInstance(
            Aggregate,
            AGGREGATE_ID,
            aggregateHistory
        )

        assertThat aggregate.numberOfAppliedEvents, equalTo(aggregateHistory.size())
    }

    List<Event> getAggregateHistory() {
        (0..2).collect {
            new Business_event_happened()
        }
    }



    static class Aggregate {
        static aggregateName = 'AGGREGATE'

        final UUID id
        int numberOfAppliedEvents = 0

        Aggregate(UUID id) {
            this.id = id
        }

        def "Business event happened"(Map event) {
            numberOfAppliedEvents++
        }
    }

    static class Business_event_happened extends BaseEvent<Aggregate> { }

}
