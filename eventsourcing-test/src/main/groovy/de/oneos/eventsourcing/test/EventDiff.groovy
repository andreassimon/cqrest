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
        if(left.eventName != right.eventName) {
            return new EventTypesDiffer(left, right)
        }
        if(left.sequenceNumber != right.sequenceNumber) {
            return new SequenceNumbersDiffer(left, right)
        }
        return new EventAttributesDiffer(left, right)
    }

}
