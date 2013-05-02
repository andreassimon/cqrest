package de.oneos.eventsourcing.test

import de.oneos.eventstore.inmemory.InMemoryEventStore
import de.oneos.eventsourcing.Event
import de.oneos.eventstore.EventStore

class PreconditionsCollector {
    InMemoryEventStore eventStore
    EventSequence eventSequence = new EventSequence()

    String application, boundedContext

    PreconditionsCollector(InMemoryEventStore eventStore, String application, String boundedContext) {
        assert eventStore != null
        assert application != null
        assert boundedContext != null

        this.eventStore = eventStore
        this.application = application
        this.boundedContext = boundedContext
    }

    ExpectationsCollector capture(Closure<?> preconditions) {
        this.with(preconditions)
        return new ExpectationsCollector(
            this.eventStore,
            this.eventStore.history.size(),
            this.eventSequence
        )
    }

    void event(UUID aggregateId, Event givenEvent) {
        eventStore.addEventEnvelope(aggregateId, this.application, this.boundedContext, givenEvent, eventSequence.next(aggregateId), EventStore.USER_UNKNOWN)
    }

}
