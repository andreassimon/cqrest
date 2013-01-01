package infrastructure

import domain.events.Event
import framework.EventPublisher

class PersistentEventPublisher implements EventPublisher {
    def eventStore

    PersistentEventPublisher(eventStore) {
        this.eventStore = eventStore
    }

    @Override
    void publish(Event<?> event) {
        eventStore.save(event)
    }
}
