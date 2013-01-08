package framework

import domain.events.Event

class TransactionalEventPublisher implements EventPublisher {
    List<EventPublisher> subordinatedEventPublishers

    @Override
    void publish(Event<?> event) {
        subordinatedEventPublishers.each {
           it.publish(event)
        }
    }
}



