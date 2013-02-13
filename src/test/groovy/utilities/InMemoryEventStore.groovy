package utilities

import infrastructure.persistence.DefaultEventStore
import domain.events.EventEnvelope

class InMemoryEventStore extends DefaultEventStore {
    def history = []

    @Override
    def save(EventEnvelope eventEnvelope) {
        throw new IllegalAccessError('Not implemented in InMemoryEventStore')
    }

    @Override
    def getEventsFor(String applicationName = '', String boundedContextName = '', String aggregateName, UUID aggregateId, String eventPackageName) {
        return history
    }

}

