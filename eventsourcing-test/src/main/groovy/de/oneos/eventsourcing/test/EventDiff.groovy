package de.oneos.eventsourcing.test

abstract class EventDiff {

    public static EventDiff of(RecordedEvent left, RecordedEvent right) {
        if (left == right) {
            return new NoDifferences(left, right)
        }
        if (left == null || right == null || left.event.eventName != right.event.eventName) {
            return new EventTypesDiffer(left, right)
        }
        return new EventAttributesDiffer(left, right)
    }

}
