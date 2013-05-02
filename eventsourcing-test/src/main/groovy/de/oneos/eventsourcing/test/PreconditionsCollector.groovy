package de.oneos.eventsourcing.test

import de.oneos.eventstore.inmemory.InMemoryEventStore
import de.oneos.eventsourcing.Event
import de.oneos.eventstore.EventStore

class PreconditionsCollector {
    InMemoryEventStore eventStore
    Map<UUID, Integer> sequenceNumbers = [:]

    String application, boundedContext

    PreconditionsCollector(InMemoryEventStore eventStore, String application, String boundedContext) {
        assert eventStore != null
        assert application != null
        assert boundedContext != null

        this.eventStore = eventStore
        this.application = application
        this.boundedContext = boundedContext
    }

    void event(UUID aggregateId, Event givenEvent) {
        eventStore.addEventEnvelope(aggregateId, this.application, this.boundedContext, givenEvent, nextSequenceNumber(aggregateId), EventStore.USER_UNKNOWN)
    }

    int nextSequenceNumber(UUID aggregateId) {
        if(!sequenceNumbers.containsKey(aggregateId)) {
            sequenceNumbers[aggregateId] = 0
        }
        sequenceNumbers[aggregateId]++
    }

    def getCollectedEvents() {
        return [
            size: eventStore.history.size()
        ]
    }
}
