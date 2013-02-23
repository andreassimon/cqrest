package de.oneos.cqrs.readmodels

interface EventSupplier {
    void subscribeTo(EventFilter eventFilter, EventProcessor eventProcessor)
}
