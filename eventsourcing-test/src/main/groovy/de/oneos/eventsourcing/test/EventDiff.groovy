package de.oneos.eventsourcing.test

abstract class EventDiff {

    public static EventDiff of(RecordedEvent left, RecordedEvent right) {
        if(left == right) {
            return new NoDifferences(left, right)
        }
        if(left == null || right == null) {
            return new EventTypesDiffer(left, right)
        }
        if(left.aggregateId != right.aggregateId) {
            return new AggregateIdsDiffer(left, right)
        }
        if(left.event.eventName != right.event.eventName) {
            return new EventTypesDiffer(left, right)
        }
        return new EventAttributesDiffer(left, right)
    }

}
