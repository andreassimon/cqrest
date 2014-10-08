package org.cqrest.eventsourcing

import groovy.transform.Canonical

@Canonical
class CorrelatedEvent {

    final String eventType

    static CorrelatedEvent create(String eventType) {
        return new CorrelatedEvent(eventType)
    }
}
