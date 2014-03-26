package de.oneos.eventselection.amqp

import de.oneos.eventsourcing.*

class StubEventSupplier implements EventSupplier {

    List<EventEnvelope> queryResult


    @Override
    @Deprecated
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
