package de.oneos.eventsourcing

interface EventSupplier {

    void subscribeTo(EventConsumer eventConsumer)
    void subscribeTo(Map<String, ?> criteria, EventConsumer eventConsumer)
    void withEventEnvelopes(Map<String, ?> criteria, Closure block)

}
