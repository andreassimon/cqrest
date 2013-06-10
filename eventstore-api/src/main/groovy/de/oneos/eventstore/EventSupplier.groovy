package de.oneos.eventstore

import de.oneos.eventstore.EventProcessor


interface EventSupplier {

    void subscribeTo(Map<String, ?> criteria, EventProcessor eventProcessor)
    void withEventEnvelopes(Map<String, ?> criteria, Closure block)

}
