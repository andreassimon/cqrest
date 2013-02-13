package framework

import domain.events.EventEnvelope

class TransactionalEventPublisher implements EventPublisher {
    List<EventPublisher> subordinatedEventPublishers

    @Override
    void publish(EventEnvelope eventEnvelope) {
        subordinatedEventPublishers.each {
           it.publish(eventEnvelope)
        }
    }
}
