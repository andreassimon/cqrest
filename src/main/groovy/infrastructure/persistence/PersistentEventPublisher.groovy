package infrastructure.persistence

import framework.EventPublisher

import domain.events.EventEnvelope
import de.oneos.cqrs.eventstore.EventStore

class PersistentEventPublisher implements EventPublisher {

    EventStore eventStore

    PersistentEventPublisher() { }

    PersistentEventPublisher(EventStore eventStore) {
        this.eventStore = eventStore
    }

    @Override
    void publish(EventEnvelope eventEnvelope) {
        eventStore.save(eventEnvelope)
    }

}
