package de.oneos.eventsourcing.test

import de.oneos.eventsourcing.Correlation
import de.oneos.eventsourcing.EventBus


class InMemoryEventBus extends EventBus {

    Collection<Correlation> subscribedCorrelations = []

    @Override
    Correlation doSubscribeCorrelation(Correlation correlation) {
        subscribedCorrelations << correlation
        return correlation
    }

    @Override
    void doEmit(UUID correlation, String eventType) {
        throw new RuntimeException('Not implemented')
    }

}
