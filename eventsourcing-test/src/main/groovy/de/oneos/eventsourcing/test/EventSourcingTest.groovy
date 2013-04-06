package de.oneos.eventsourcing.test

import org.junit.*

import de.oneos.eventstore.inmemory.*
import static de.oneos.eventstore.EventStore.*


abstract class EventSourcingTest {

    int numberOfGivenEvents = 0
    InMemoryEventStore eventStore
    String application, boundedContext

    EventSourcingTest(String application, String boundedContext) {
        assert application != null
        assert boundedContext != null

        this.application = application
        this.boundedContext = boundedContext
    }

    @Before
    void setUp() {
        eventStore = new InMemoryEventStore()
        numberOfGivenEvents = 0
    }

    protected given(Closure<?> preconditions) {
        def collector = new PreconditionsCollector(eventStore, application, boundedContext)
        collector.with preconditions
        numberOfGivenEvents = collector.collectedEvents.size
    }

    protected when(Closure<?> action) {
        eventStore.inUnitOfWork(application, boundedContext, NO_CORRELATION_ID, USER_UNKNOWN, action)
    }

    protected then(Closure<?> expectations) {
        def collector = new ExpectationsCollector(eventStore)
        collector.assertAreMet(expectations, numberOfGivenEvents)
    }

}
