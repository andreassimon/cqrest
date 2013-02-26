package de.oneos.eventstore

import static java.util.UUID.randomUUID

import org.junit.*
import static org.junit.Assert.*
import static org.mockito.Mockito.*
import static de.oneos.Matchers.*
import static de.oneos.Stubbing.*

import de.oneos.eventsourcing.*


class UnitOfWorkTest {
    static final String APPLICATION_NAME = 'APPLICATION_NAME'
    static final String BOUNDED_CONTEXT_NAME = 'BOUNDED_CONTEXT_NAME'
    static final String AGGREGATE_NAME = 'AGGREGATE_NAME'

    static UUID AGGREGATE_ID = randomUUID()
    static UUID ANOTHER_AGGREGATE_ID = randomUUID()

    UnitOfWork unitOfWork
    TestableClosure callback, eventFactory
    EventStore eventStore

    @Before
    void setUp() {
        callback = new TestableClosure(this)
        eventFactory = new TestableClosure(this)

        while(ANOTHER_AGGREGATE_ID == AGGREGATE_ID) {
            ANOTHER_AGGREGATE_ID == randomUUID()
        }

        eventStore = mock(EventStore)
    }


    @Test
    void should_collect_published_events() {
        unitOfWork = new UnitOfWork(eventStore)

        unitOfWork.publishEvent(APPLICATION_NAME, BOUNDED_CONTEXT_NAME, AGGREGATE_NAME, AGGREGATE_ID, new Business_event_happened())

        unitOfWork.eachEventEnvelope(callback)

        assertThat 'callback', callback, wasCalledOnceWith() { EventEnvelope envelope ->
            envelope.applicationName == APPLICATION_NAME &&
            envelope.boundedContextName == BOUNDED_CONTEXT_NAME &&
            envelope.aggregateName == AGGREGATE_NAME &&
            envelope.aggregateId == AGGREGATE_ID &&
            envelope.event == new Business_event_happened()
        }
    }

    @Test
    void should_increment_the_sequenceNumber_for_published_events_for_an_aggregate() {
        unitOfWork = new UnitOfWork(eventStore)

        2.times {
            publishEvent(unitOfWork, AGGREGATE_ID)
        }

        unitOfWork.eachEventEnvelope(callback)

        assertThat 'callback', callback, wasCalledOnceWith() { envelope -> 0 == envelope.sequenceNumber }
        assertThat 'callback', callback, wasCalledOnceWith() { envelope -> 1 == envelope.sequenceNumber }
    }

    @Test
    void should_increment_the_sequenceNumber_depending_on_the_aggregate() {
        unitOfWork = new UnitOfWork(eventStore)

        publishEvent(unitOfWork, AGGREGATE_ID)
        publishEvent(unitOfWork, ANOTHER_AGGREGATE_ID)

        unitOfWork.eachEventEnvelope(callback)

        assertThat 'callback', callback, wasCalledOnceWith() { envelope -> AGGREGATE_ID == envelope.aggregateId && 0 == envelope.sequenceNumber }
        assertThat 'callback', callback, wasCalledOnceWith() { envelope -> ANOTHER_AGGREGATE_ID == envelope.aggregateId && 0 == envelope.sequenceNumber }
    }

    protected publishEvent(UnitOfWork unitOfWork, UUID aggregateId) {
        unitOfWork.publishEvent(APPLICATION_NAME, BOUNDED_CONTEXT_NAME, AGGREGATE_NAME, aggregateId, new Business_event_happened())
    }

    @Test
    void should_build_aggregates_from_events() {
        unitOfWork = new UnitOfWork(eventStore)

        unitOfWork.get(Aggregate, APPLICATION_NAME, BOUNDED_CONTEXT_NAME, AGGREGATE_NAME, AGGREGATE_ID, eventFactory)

        verify(eventStore).loadEventEnvelopes(APPLICATION_NAME, BOUNDED_CONTEXT_NAME, AGGREGATE_NAME, AGGREGATE_ID, eventFactory)
    }

    @Test
    void should_add_dynamic_method_publishEvent_to_loaded_aggregates() {
        unitOfWork = new UnitOfWork(eventStore)

        loadAggregate(unitOfWork, AGGREGATE_ID).publishEvent(new Business_event_happened())
        loadAggregate(unitOfWork, ANOTHER_AGGREGATE_ID).publishEvent(new Business_event_happened())
        unitOfWork.eachEventEnvelope(callback)

        assertThat 'callback', callback, wasCalledOnceWith(aggregateId: AGGREGATE_ID);
        assertThat 'callback', callback, wasCalledOnceWith(aggregateId: ANOTHER_AGGREGATE_ID);
        assertThat 'callback', callback, wasCalledTwiceWith(
            applicationName: APPLICATION_NAME,
            boundedContextName: BOUNDED_CONTEXT_NAME,
            aggregateName: AGGREGATE_NAME,
            event: new Business_event_happened()
        )
    }

    protected loadAggregate(UnitOfWork unitOfWork, UUID aggregateId) {
        unitOfWork.get(Aggregate, APPLICATION_NAME, BOUNDED_CONTEXT_NAME, AGGREGATE_NAME, aggregateId, eventFactory)
    }

    @Test
    void should_update_the_next_sequenceNumber_for_loaded_aggregates() {
        int nextSequenceNumber = 0
        unitOfWork = new UnitOfWork(eventStore)
        when(eventStore.loadEventEnvelopes(eq(APPLICATION_NAME), eq(BOUNDED_CONTEXT_NAME), eq(AGGREGATE_NAME), eq(AGGREGATE_ID), any(Closure))).then(answer {[
            newEventEnvelope(sequenceNumber: nextSequenceNumber++),
            newEventEnvelope(sequenceNumber: nextSequenceNumber++),
            newEventEnvelope(sequenceNumber: nextSequenceNumber++)
        ]})

        loadAggregate(unitOfWork, AGGREGATE_ID).publishEvent(new Business_event_happened())

        unitOfWork.eachEventEnvelope(callback)
        assertThat 'callback', callback, wasCalledOnceWith(sequenceNumber: nextSequenceNumber);
    }

    static newEventEnvelope(Map<String, Integer> attributes) {
        new EventEnvelope(APPLICATION_NAME, BOUNDED_CONTEXT_NAME, AGGREGATE_NAME, AGGREGATE_ID, new Business_event_happened(), attributes['sequenceNumber'])
    }


    static class Aggregate {

    }

    static class Business_event_happened extends Event {
        @Override
        def applyTo(aggregate) { aggregate }
    }
}
