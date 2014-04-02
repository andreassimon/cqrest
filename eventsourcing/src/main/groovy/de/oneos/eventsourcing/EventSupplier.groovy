package de.oneos.eventsourcing


@Deprecated
interface EventSupplier {

    @Deprecated
    void subscribeTo(Map<String, ?> criteria, EventConsumer eventConsumer)
    @Deprecated
    void withEventEnvelopes(Map<String, ?> criteria, Closure block)

}
