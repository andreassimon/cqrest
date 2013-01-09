package utilities

import domain.events.Event
import infrastructure.Repository

class InMemoryRepository implements Repository {
    def history = []

    def getEventsFor(Class aggregateClass, UUID aggregateId) {
        return history
    }
}

class InMemoryEventPublisher {
    def receivedEvents = []

    void publish(Event<?> event) {
        receivedEvents << event
    }

}
