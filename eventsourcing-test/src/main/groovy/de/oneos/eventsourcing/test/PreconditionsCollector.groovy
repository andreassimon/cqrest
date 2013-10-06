package de.oneos.eventsourcing.test

import de.oneos.eventstore.inmemory.InMemoryEventStore
import de.oneos.eventsourcing.Event


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
        event('UNKNOWN', aggregateId, givenEvent)
    }

    void event(String aggregateType, UUID aggregateId, Event givenEvent) {
        eventStore.addEventEnvelope(aggregateId, this.application, this.boundedContext, aggregateType, givenEvent, eventSequence.next(aggregateId))
    }

}
