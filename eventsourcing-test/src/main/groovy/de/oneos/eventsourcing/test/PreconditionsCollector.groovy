package de.oneos.eventsourcing.test

import de.oneos.eventstore.inmemory.InMemoryEventStore
import de.oneos.eventsourcing.Event
import de.oneos.eventstore.EventStore

class PreconditionsCollector {
    InMemoryEventStore eventStore

    String application, boundedContext

    PreconditionsCollector(InMemoryEventStore eventStore, String application, String boundedContext) {
        assert eventStore != null
        assert application != null
        assert boundedContext != null

        this.eventStore = eventStore
        this.application = application
        this.boundedContext = boundedContext
    }

    void event(UUID aggregateId, int sequenceNumber, Event event) {
        eventStore.addEventEnvelope(aggregateId, this.application, this.boundedContext, event, sequenceNumber, EventStore.USER_UNKNOWN)
    }

    def getCollectedEvents() {
        return [
            size: eventStore.history.size()
        ]
    }
}
