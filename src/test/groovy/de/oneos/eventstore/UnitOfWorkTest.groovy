package de.oneos.eventstore

import static java.util.UUID.randomUUID

import org.junit.*
import static org.junit.Assert.*
import static org.hamcrest.Matchers.*
import static org.mockito.Mockito.*

import de.oneos.eventsourcing.*


class UnitOfWorkTest {
    static final String APPLICATION_NAME = 'APPLICATION_NAME'
    static final String BOUNDED_CONTEXT_NAME = 'BOUNDED_CONTEXT_NAME'
    static final String AGGREGATE_NAME = 'AGGREGATE_NAME'
    static final UUID   AGGREGATE_ID = randomUUID()

    @Test
    void should_collect_published_events() {
        int callCount = 0
        UnitOfWork unitOfWork = new UnitOfWork()

        unitOfWork.publishEvent(APPLICATION_NAME, BOUNDED_CONTEXT_NAME, AGGREGATE_NAME, AGGREGATE_ID, new Business_event_happened())

        unitOfWork.eachEventEnvelope { EventEnvelope eventEnvelope ->
            callCount++
            assertThat eventEnvelope.applicationName, equalTo(APPLICATION_NAME)
            assertThat eventEnvelope.boundedContextName, equalTo(BOUNDED_CONTEXT_NAME)
            assertThat eventEnvelope.aggregateName, equalTo(AGGREGATE_NAME)
            assertThat eventEnvelope.aggregateId, equalTo(AGGREGATE_ID)
            assertThat eventEnvelope.event, equalTo(new Business_event_happened())
        }

        assertThat 'Callback should have been called', callCount, equalTo(1)
    }
    // TODO increment sequenceNumber for new events
    // TODO iterate over all published events
    // TODO load aggregates?


    static class Business_event_happened extends Event {
        @Override
        def applyTo(aggregate) { aggregate }
    }
}
