package de.oneos.eventstore

import static java.util.UUID.randomUUID

import org.junit.*
import static org.junit.Assert.*
import static org.mockito.Mockito.*
import static org.hamcrest.Matchers.*
import static de.oneos.Matchers.*
import static de.oneos.Stubbing.*

import de.oneos.eventsourcing.*
import de.oneos.validation.*


class UnitOfWorkTest {
    static final String APPLICATION_NAME = 'APPLICATION_NAME'
    static final String BOUNDED_CONTEXT_NAME = 'BOUNDED_CONTEXT_NAME'
    static final String AGGREGATE_NAME = 'AGGREGATE_NAME'

    static UUID NO_CORRELATION_ID = null
    static String USER_UNKNOWN = null

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

        unitOfWork = new UnitOfWork(eventStore, APPLICATION_NAME, BOUNDED_CONTEXT_NAME, NO_CORRELATION_ID, USER_UNKNOWN)
    }


    @Test
    void should_collect_new_events_from_attached_aggregates() {
        Aggregate aggregate = new Aggregate(AGGREGATE_ID)
        aggregate.emit(new Business_event_happened())

        unitOfWork.attach(aggregate)

        unitOfWork.eachEventEnvelope(callback)
        assertThat 'callback', callback, wasCalledOnceWith(EventEnvelope, [
            applicationName: unitOfWork.applicationName,
            boundedContextName: unitOfWork.boundedContextName,
            aggregateName: aggregate.aggregateName,
            aggregateId: aggregate.id,
            event: new Business_event_happened()
        ])
    }

    @Test
    void should_pass_over_aggregates_without_new_events() {
        Aggregate aggregate = new Aggregate(AGGREGATE_ID)

        unitOfWork.attach(aggregate)

        unitOfWork.eachEventEnvelope(callback)
        assertThat 'callback', callback, wasNeverCalled()
    }

    @Test
    void should_increment_the_sequenceNumber_for_published_events_for_an_aggregate() {
        Aggregate aggregate = new Aggregate(AGGREGATE_ID)
        2.times {
            aggregate.emit(new Business_event_happened())
        }
        unitOfWork.attach(aggregate)

        unitOfWork.eachEventEnvelope(callback)

        assertThat 'callback', callback, wasCalledOnceWith(EventEnvelope, [sequenceNumber: 0])
        assertThat 'callback', callback, wasCalledOnceWith(EventEnvelope, [sequenceNumber: 1])
    }

    @Test
    void should_increment_the_sequenceNumber_depending_on_the_aggregate() {
        [AGGREGATE_ID, ANOTHER_AGGREGATE_ID].each { UUID id ->
            Aggregate aggregate = new Aggregate(id)
            aggregate.emit(new Business_event_happened())
            unitOfWork.attach(aggregate)
        }

        unitOfWork.eachEventEnvelope(callback)

        assertThat 'callback', callback, wasCalledOnceWith(EventEnvelope, [aggregateId: AGGREGATE_ID, sequenceNumber: 0])
        assertThat 'callback', callback, wasCalledOnceWith(EventEnvelope, [aggregateId: ANOTHER_AGGREGATE_ID, sequenceNumber: 0])
    }

    @Test
    void should_flush_attached_aggregates() {
        Aggregate aggregate = new Aggregate(AGGREGATE_ID)
        unitOfWork.attach(aggregate)
        2.times { aggregate.emit(new Business_event_happened()) }
        assertThat aggregate.newEvents.size(), equalTo(2)

        unitOfWork.flush()

        assertThat aggregate.newEvents, empty()
    }


    @Test
    void should_create_aggregate_instances_with_AggregateFactory() {
        // TODO Das Test-Setup ist zu kompliziert; das stinkt nach schlechtem Design
        when(eventStore.loadEventEnvelopes(AGGREGATE_ID)).then(answer {
            [new EventEnvelope(
                APPLICATION_NAME,
                BOUNDED_CONTEXT_NAME,
                Aggregate.aggregateName,
                AGGREGATE_ID,
                new Business_event_happened(),
                NO_CORRELATION_ID,
                USER_UNKNOWN
            )]
        })
        unitOfWork.aggregateFactory = aggregateFactory
        when(aggregateFactory.newInstance(Aggregate, AGGREGATE_ID, [new Business_event_happened()])).thenReturn DUMMY_AGGREGATE

        def actualAggregate = unitOfWork.get(Aggregate, AGGREGATE_ID)

        assertThat actualAggregate, equalTo(DUMMY_AGGREGATE)
    }


    static final int LAST_SEQUENCE_NUMBER = 0

    @Test
    void should_update_the_next_sequenceNumber_for_loaded_aggregates() {
        when(eventStore.loadEventEnvelopes(AGGREGATE_ID)).then(answer {
            (0..LAST_SEQUENCE_NUMBER).collect { newEventEnvelope(sequenceNumber: it) }
        })
        Aggregate aggregate = unitOfWork.get(Aggregate, AGGREGATE_ID)

        aggregate.emit(new Business_event_happened())

        unitOfWork.eachEventEnvelope(callback)
        assertThat 'callback', callback, wasCalledOnceWith(EventEnvelope, [sequenceNumber: LAST_SEQUENCE_NUMBER + 1]);
    }


    @Test(expected=ValidationException)
    void should_throw_an_Exception_when_any_aggregate_is_invalid() {
        unitOfWork.attach(new ValidAggregate()).emit(new Business_event_happened())
        unitOfWork.attach(new ValidAggregate()).emit(new Business_event_happened())
        unitOfWork.attach(new InvalidAggregate()).emit(new Business_event_happened())

        unitOfWork.eachEventEnvelope { println(it) }
    }

    @Test
    void should_not_throw_an_Exception_when_an_aggregate_cannot_be_validated() {
        unitOfWork.attach(new NotValidatableAggregate(AGGREGATE_ID)).emit(new Business_event_happened())

        unitOfWork.eachEventEnvelope(callback)

        assertThat 'callback', callback, wasCalledOnceWith(EventEnvelope, [aggregateId: AGGREGATE_ID])
    }

    @Test
    void should_not_provide_any_event_when_any_aggregate_is_invalid() {
        unitOfWork.attach(new NotValidatableAggregate(AGGREGATE_ID)).emit(new Business_event_happened())
        unitOfWork.attach(new InvalidAggregate()).emit(new Business_event_happened())

        try {
            unitOfWork.eachEventEnvelope(callback)
        } catch(ValidationException) {}

        assertThat 'callback', callback, wasNeverCalled()
    }


    static class NotValidatableAggregate {
        static { NotValidatableAggregate.mixin(EventSourcing) }
        static aggregateName = AGGREGATE_NAME

        final UUID id
        int numberOfAppliedEvents = 0

        NotValidatableAggregate(UUID id) {
            this.id = id
        }
    }

    static class Aggregate extends NotValidatableAggregate {
        Aggregate(UUID id) { super(id) }
    }

    protected static newEventEnvelope(Map<String, Integer> attributes) {
        new EventEnvelope(APPLICATION_NAME, BOUNDED_CONTEXT_NAME, AGGREGATE_NAME, AGGREGATE_ID, new Business_event_happened(), attributes['sequenceNumber'], NO_CORRELATION_ID, USER_UNKNOWN)
    }

    static class InvalidAggregate implements Validatable<InvalidAggregate> {
        static { InvalidAggregate.mixin(EventSourcing) }
        boolean isValid() { return false }
        String validationMessage() { return 'will never be valid' }
        String toString() { 'InvalidAggregate' }
    }

    static class ValidAggregate implements Validatable<ValidAggregate> {
        static { ValidAggregate.mixin(EventSourcing) }
        boolean isValid() { return true }
        String validationMessage() { if(isValid()) return ''; throw new IllegalStateException('Should always be valid') }
        String toString() { 'ValidAggregate' }
    }


    static class Business_event_happened<A> extends BaseEvent<A> {
        @Override
        void applyTo(A aggregate) { }
    }
}
