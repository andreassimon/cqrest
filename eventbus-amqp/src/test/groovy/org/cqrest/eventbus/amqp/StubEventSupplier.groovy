package org.cqrest.eventbus.amqp

import org.cqrest.eventsourcing.*

class StubEventSupplier implements EventSupplier {

    List<EventEnvelope> queryResult


    @Override
    void subscribeTo(Map<String, ?> criteria, EventConsumer eventConsumer) {
        throw new RuntimeException('StubEventSupplier.subscribeTo() is not implemented')
    }

    @Override
    void withEventEnvelopes(Map<String, ?> criteria, Closure block) {
        queryResult.each(block)
    }

}
