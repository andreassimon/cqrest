package framework

import domain.events.Event

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
