package org.cqrest.eventsourcing.test

import org.cqrest.eventsourcing.Correlation
import org.cqrest.eventsourcing.EventBus


class InMemoryEventBus implements EventBus {

    Collection<Correlation> subscribedCorrelations = []

    @Override
    Correlation subscribeCorrelation(Correlation correlation) {
        subscribedCorrelations << correlation
        return correlation
    }

    @Override
    void emit(UUID correlation, String eventType) {
        throw new RuntimeException('Not implemented')
    }

}
