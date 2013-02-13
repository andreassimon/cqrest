package utilities

import domain.events.EventEnvelope
import framework.EventPublisher

class InMemoryEventPublisher implements EventPublisher {
    def receivedEventEnvelopes = []

    void publish(EventEnvelope eventEnvelope) {
        receivedEventEnvelopes << eventEnvelope.event
    }

}
