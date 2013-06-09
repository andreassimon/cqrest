package de.oneos.eventselection

import de.oneos.eventstore.EventProcessor


interface EventSupplier {
    void subscribeTo(EventFilter eventFilter, EventProcessor eventProcessor)
}
