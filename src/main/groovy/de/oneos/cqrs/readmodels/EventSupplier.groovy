package de.oneos.cqrs.readmodels

interface EventSupplier {
    void subscribeTo(Map eventFilter, EventProcessor eventProcessor)
}
