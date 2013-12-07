package de.oneos.eventstore

import de.oneos.eventsourcing.*


interface EventSupplier {

    EventSupplier rightShift(EventConsumer eventConsumer)
    void subscribeTo(EventConsumer eventConsumer)
    void subscribeTo(Map<String, ?> criteria, EventConsumer eventConsumer)
    void withEventEnvelopes(Map<String, ?> criteria, Closure block)
    de.oneos.projections.Observable<EventEnvelope> observe(Map<String, ?> criteria)

}
