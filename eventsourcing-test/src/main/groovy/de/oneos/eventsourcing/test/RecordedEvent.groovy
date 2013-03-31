package de.oneos.eventsourcing.test

import de.oneos.eventsourcing.Event

class RecordedEvent {

    UUID aggregateId
    int sequenceNumber
    Event event

    @Override
    boolean equals(Object that) {
        RecordedEvent == that.getClass() &&
        this.aggregateId == that.aggregateId &&
        this.sequenceNumber == that.sequenceNumber &&
        this.event == that.event
    }

}
