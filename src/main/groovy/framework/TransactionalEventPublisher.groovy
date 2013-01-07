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

class LoopbackEventPublisher implements EventPublisher {

    def aggregate

    LoopbackEventPublisher(aggregate) {
        this.aggregate = aggregate
    }

    @Override
    void publish(Event<?> event) {
        aggregate.apply(event)
    }
}


