package de.oneos.eventsourcing.test

import de.oneos.eventstore.inmemory.InMemoryEventStore
import de.oneos.eventsourcing.Event
import de.oneos.eventstore.EventStore

class PreconditionsCollector {
    InMemoryEventStore eventStore

    PreconditionsCollector(InMemoryEventStore eventStore) {
        this.eventStore = eventStore
    }

    void event(UUID aggregateId, int sequenceNumber, Event event) {
        eventStore.addEventEnvelope(aggregateId, event, sequenceNumber, EventStore.USER_UNKNOWN)
    }

    def getCollectedEvents() {
        return [
            size: eventStore.history.size()
        ]
    }
}
