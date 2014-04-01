package de.oneos.eventsourcing

interface EventSupplier {

    @Deprecated
    void subscribeTo(Map<String, ?> criteria, EventConsumer eventConsumer)
    @Deprecated
    void withEventEnvelopes(Map<String, ?> criteria, Closure block)

}
