package de.oneos.eventsourcing.test

import de.oneos.eventsourcing.Correlation
import de.oneos.eventsourcing.EventBus


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
