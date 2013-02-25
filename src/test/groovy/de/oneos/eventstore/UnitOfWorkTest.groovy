package de.oneos.eventstore

import static java.util.UUID.randomUUID

import org.junit.*
import static org.junit.Assert.*
import static de.oneos.Matchers.*

import de.oneos.eventsourcing.*


class UnitOfWorkTest {
    static final String APPLICATION_NAME = 'APPLICATION_NAME'
    static final String BOUNDED_CONTEXT_NAME = 'BOUNDED_CONTEXT_NAME'
    static final String AGGREGATE_NAME = 'AGGREGATE_NAME'
    static final UUID   AGGREGATE_ID = randomUUID()

    UnitOfWork unitOfWork
    TestableClosure callback

    @Before
    void setUp() {
        callback = new TestableClosure(this)
    }


    @Test
    void should_collect_published_events() {
        unitOfWork = new UnitOfWork()

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
        unitOfWork = new UnitOfWork()

        2.times {
            unitOfWork.publishEvent(APPLICATION_NAME, BOUNDED_CONTEXT_NAME, AGGREGATE_NAME, AGGREGATE_ID, new Business_event_happened())
        }

        unitOfWork.eachEventEnvelope(callback)

        assertThat 'callback', callback, wasCalledOnceWith() { envelope -> 0 == envelope.sequenceNumber }
        assertThat 'callback', callback, wasCalledOnceWith() { envelope -> 1 == envelope.sequenceNumber }
    }

    // TODO iterate over all published events
    // TODO load aggregates?


    static class Business_event_happened extends Event {
        @Override
        def applyTo(aggregate) { aggregate }
    }
}
