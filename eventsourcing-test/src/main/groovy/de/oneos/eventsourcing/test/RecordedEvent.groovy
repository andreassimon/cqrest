package de.oneos.eventsourcing.test

import de.oneos.eventsourcing.Event

class RecordedEvent {

    UUID aggregateId
    int sequenceNumber
    def eventName
    def eventAttributes

    @Override
    boolean equals(Object that) {
        RecordedEvent == that.getClass() &&
        this.aggregateId == that.aggregateId &&
        this.sequenceNumber == that.sequenceNumber &&
        this.eventName == that.eventName &&
        this.eventAttributes == that.eventAttributes
    }

}
