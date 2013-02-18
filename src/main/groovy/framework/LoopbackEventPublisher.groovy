package framework

import domain.events.EventEnvelope

class LoopbackEventPublisher implements EventPublisher {

    def aggregate

    LoopbackEventPublisher(aggregate) {
        this.aggregate = aggregate
    }

    @Override
    void publish(EventEnvelope eventEnvelope) {
        eventEnvelope.applyEventTo(aggregate)
    }
}
