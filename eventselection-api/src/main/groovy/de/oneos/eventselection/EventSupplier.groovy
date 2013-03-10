package de.oneos.eventselection

interface EventSupplier {
    void subscribeTo(EventFilter eventFilter, EventProcessor eventProcessor)
}
