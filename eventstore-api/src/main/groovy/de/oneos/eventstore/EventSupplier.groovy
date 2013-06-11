package de.oneos.eventstore


interface EventSupplier {

    void subscribeTo(Map<String, ?> criteria, EventProcessor eventProcessor)
    void withEventEnvelopes(Map<String, ?> criteria, Closure block)

}
