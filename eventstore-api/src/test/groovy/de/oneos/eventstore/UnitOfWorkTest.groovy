package de.oneos.eventstore

import static java.util.UUID.randomUUID

import org.junit.*
import static org.junit.Assert.*
import static org.mockito.Mockito.*
import static org.hamcrest.Matchers.*
import static de.oneos.Matchers.*
import static de.oneos.Stubbing.*

import de.oneos.eventsourcing.*


class UnitOfWorkTest {
    static final String APPLICATION_NAME = 'APPLICATION_NAME'
    static final String BOUNDED_CONTEXT_NAME = 'BOUNDED_CONTEXT_NAME'
    static final String AGGREGATE_NAME = 'AGGREGATE_NAME'

    static final int LAST_SEQUENCE_NUMBER = 2
    static UUID AGGREGATE_ID = randomUUID()
    static UUID ANOTHER_AGGREGATE_ID = randomUUID()
    static final Aggregate DUMMY_AGGREGATE = new Aggregate(randomUUID()) {
        String toString() { 'DUMMY AGGREGATE' }

        boolean equals(Object that) { this.toString() == that.toString() }
    }

    UnitOfWork unitOfWork
    TestableClosure callback, eventFactory
    EventStore eventStore
    AggregateFactory aggregateFactory

    @Before
    void setUp() {
        callback = new TestableClosure(this)
        eventFactory = new TestableClosure(this)

        while(ANOTHER_AGGREGATE_ID == AGGREGATE_ID) {
            ANOTHER_AGGREGATE_ID == randomUUID()
        }

        eventStore = mock(EventStore)
        aggregateFactory = mock(AggregateFactory)

        unitOfWork = new UnitOfWork(eventStore)
    }


    @Test
    void should_collect_published_events() {
        unitOfWork.publishEvent(APPLICATION_NAME, BOUNDED_CONTEXT_NAME, AGGREGATE_NAME, AGGREGATE_ID, new Business_event_happened())

        unitOfWork.eachEventEnvelope(callback)
        assertThat 'callback', callback, wasCalledOnceWith(EventEnvelope, [
            applicationName: APPLICATION_NAME,
            boundedContextName: BOUNDED_CONTEXT_NAME,
            aggregateName: AGGREGATE_NAME,
            aggregateId: AGGREGATE_ID,
            event: new Business_event_happened()
        ])
    }

    @Test
    void should_increment_the_sequenceNumber_for_published_events_for_an_aggregate() {
        2.times {
            publishEvent(unitOfWork, AGGREGATE_ID)
        }

        unitOfWork.eachEventEnvelope(callback)

        assertThat 'callback', callback, wasCalledOnceWith(EventEnvelope, [sequenceNumber: 0])
        assertThat 'callback', callback, wasCalledOnceWith(EventEnvelope, [sequenceNumber: 1])
    }

    @Test
    void should_increment_the_sequenceNumber_depending_on_the_aggregate() {
        publishEvent(unitOfWork, AGGREGATE_ID)
        publishEvent(unitOfWork, ANOTHER_AGGREGATE_ID)

        unitOfWork.eachEventEnvelope(callback)

        assertThat 'callback', callback, wasCalledOnceWith(EventEnvelope, [aggregateId: AGGREGATE_ID, sequenceNumber: 0])
        assertThat 'callback', callback, wasCalledOnceWith(EventEnvelope, [aggregateId: ANOTHER_AGGREGATE_ID, sequenceNumber: 0])
    }

    protected publishEvent(UnitOfWork unitOfWork, UUID aggregateId) {
        unitOfWork.publishEvent(APPLICATION_NAME, BOUNDED_CONTEXT_NAME, AGGREGATE_NAME, aggregateId, new Business_event_happened())
    }

    protected static List<EventEnvelope> listOfEvents(eventProperties = [:]) {
        (0..2).collect { sequenceNumber ->
            new EventEnvelope<Aggregate>(APPLICATION_NAME,
                BOUNDED_CONTEXT_NAME,
                AGGREGATE_NAME,
                AGGREGATE_ID,
                new Business_event_happened(eventProperties),
                sequenceNumber)
        }
    }

    @Test
    void should_create_aggregate_instances_with_AggregateFactory() {
        // TODO Das Test-Setup ist zu kompliziert; das stinkt nach schlechtem Design
        when(eventStore.loadEventEnvelopes(eq(APPLICATION_NAME), eq(BOUNDED_CONTEXT_NAME), eq(AGGREGATE_NAME), eq(AGGREGATE_ID), any(Closure))).then(answer {
            [new EventEnvelope(
                Aggregate.applicationName,
                Aggregate.boundedContextName,
                Aggregate.aggregateName,
                AGGREGATE_ID,
                new Business_event_happened())]
        })
        unitOfWork.aggregateFactory = aggregateFactory
        when(aggregateFactory.newInstance(Aggregate, AGGREGATE_ID, [new Business_event_happened()])).thenReturn DUMMY_AGGREGATE

        def actualAggregate = unitOfWork.get(Aggregate, AGGREGATE_ID, eventFactory)

        assertThat actualAggregate, equalTo(DUMMY_AGGREGATE)
    }


    @Test
    void should_update_the_next_sequenceNumber_for_loaded_aggregates() {
        when(eventStore.loadEventEnvelopes(eq(APPLICATION_NAME), eq(BOUNDED_CONTEXT_NAME), eq(AGGREGATE_NAME), eq(AGGREGATE_ID), any(Closure))).then(answer {
            (0..LAST_SEQUENCE_NUMBER).collect { newEventEnvelope(sequenceNumber: it) }
        })
        unitOfWork.get(Aggregate, AGGREGATE_ID, eventFactory)

        publishEvent(unitOfWork, AGGREGATE_ID)

        unitOfWork.eachEventEnvelope(callback)
        assertThat 'callback', callback, wasCalledOnceWith(sequenceNumber: LAST_SEQUENCE_NUMBER + 1);
    }

    protected static newEventEnvelope(Map<String, Integer> attributes) {
        new EventEnvelope(APPLICATION_NAME, BOUNDED_CONTEXT_NAME, AGGREGATE_NAME, AGGREGATE_ID, new Business_event_happened(), attributes['sequenceNumber'])
    }


    static class Aggregate {
        static applicationName = APPLICATION_NAME
        static boundedContextName = BOUNDED_CONTEXT_NAME
        static aggregateName = AGGREGATE_NAME

        final UUID id
        int numberOfAppliedEvents = 0

        Aggregate(UUID id) {
            this.id = id
        }
    }

    static class Business_event_happened extends Event<Aggregate> {
        static { UNSERIALIZED_PROPERTIES << 'function' }
        Closure<Void> function = {}

        @Override
        void applyTo(Aggregate aggregate) {
            function(aggregate)
        }
    }
}