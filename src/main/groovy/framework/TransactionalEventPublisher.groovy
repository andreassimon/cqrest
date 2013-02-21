package framework

import domain.events.EventEnvelope

class TransactionalEventPublisher implements EventPublisher {
    List<EventPublisher> subordinatedEventPublishers

    @Override
    void publish(EventEnvelope eventEnvelope) throws PublishingException {
        subordinatedEventPublishers.each {
           it.publish(eventEnvelope)
        }
    }
}
