package de.oneos.eventsourcing.test

import org.junit.*

import de.oneos.eventsourcing.EventBus

import de.oneos.eventstore.inmemory.*
import static de.oneos.eventstore.EventStore.*


abstract class EventSourcingTest {

    InMemoryEventStore eventStore
    String application, boundedContext
    PreconditionsCollector preconditionsCollector
    ExpectationsCollector expectationsCollector

    EventSourcingTest(String application, String boundedContext) {
        assert application != null
        assert boundedContext != null

        this.application = application
        this.boundedContext = boundedContext
    }

    @Before
    void setUp() {
        EventBus.INSTANCE = new InMemoryEventBus()
        eventStore = new InMemoryEventStore()
        preconditionsCollector = new PreconditionsCollector(eventStore, application, boundedContext)
    }

    protected given(Closure<?> preconditions) {
        expectationsCollector = preconditionsCollector.capture(preconditions)
    }

    protected when(Closure<?> action) {
        inUnitOfWork(action)
    }

    protected inUnitOfWork(Closure<?> action) {
        eventStore.inUnitOfWork(application, boundedContext, NO_CORRELATION_ID, USER_UNKNOWN, action)
    }

    protected then(Closure<?> expectations) {
        if(null == expectationsCollector) {
            throw new AssertionError('Your test method must invoke `given { }` before invoking `then { }`')
        }
        expectationsCollector.assertAreMet(expectations)
    }

}
