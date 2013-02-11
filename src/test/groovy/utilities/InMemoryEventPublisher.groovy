package utilities

import domain.events.Event
import framework.EventPublisher

class InMemoryEventPublisher implements EventPublisher {
    def receivedEvents = []

    void publish(Event event) {
        receivedEvents << event
    }

}
