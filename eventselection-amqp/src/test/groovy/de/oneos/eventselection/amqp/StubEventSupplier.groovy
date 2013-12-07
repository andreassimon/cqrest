package de.oneos.eventselection.amqp

import de.oneos.eventsourcing.*
import de.oneos.eventstore.*
import de.oneos.eventstore.ObservableEventSupplier

class StubEventSupplier extends ObservableEventSupplier implements EventSupplier {

    List<EventEnvelope> queryResult

    @Override
    EventSupplier rightShift(EventConsumer eventConsumer) {
        subscribeTo(eventConsumer)
        return this
    }

    @Override
    void subscribeTo(EventConsumer eventConsumer) {
        throw new RuntimeException('StubEventSupplier.subscribeTo() is not implemented')
    }

    @Override
    void subscribeTo(Map<String, ?> criteria, EventConsumer eventConsumer) {
        throw new RuntimeException('StubEventSupplier.subscribeTo() is not implemented')
    }

    @Override
    void withEventEnvelopes(Map<String, ?> criteria, Closure block) {
        queryResult.each(block)
    }

}
