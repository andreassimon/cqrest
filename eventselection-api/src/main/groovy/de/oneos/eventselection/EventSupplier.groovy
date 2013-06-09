package de.oneos.eventselection

import de.oneos.eventstore.EventPublisher


interface EventSupplier {
    void subscribeTo(EventFilter eventFilter, EventPublisher eventPublisher)
}
