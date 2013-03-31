package de.oneos.eventsourcing.test

import org.junit.*

import de.oneos.eventstore.inmemory.*
import static de.oneos.eventstore.EventStore.*


abstract class EventSourcingTest {

    int numberOfGivenEvents = 0
    InMemoryEventStore eventStore

    @Before
    void setUp() {
        eventStore = new InMemoryEventStore('Web2Print', 'Printmedia Configuration')
        numberOfGivenEvents = 0
    }

    protected given(Closure<?> preconditions) {
        def collector = new PreconditionsCollector(eventStore)
        collector.with preconditions
        numberOfGivenEvents = collector.collectedEvents.size
    }

    protected when(Closure<?> action) {
        eventStore.inUnitOfWork(NO_CORRELATION_ID, USER_UNKNOWN, action)
    }

    protected then(Closure<?> expectations) {
        def collector = new ExpectationsCollector(eventStore)
        collector.assertAreMet(expectations, numberOfGivenEvents)
    }

}
