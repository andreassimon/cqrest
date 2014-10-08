package org.cqrest.eventsourcing.test

class AggregateIdsDiffer extends EventDiff {
    RecordedEvent left
    RecordedEvent right

    AggregateIdsDiffer(RecordedEvent left, RecordedEvent right) {
        assert left != null
        assert right != null

        this.left = left
        this.right = right
    }

    @Override
    String toString() {
        """Aggregate IDs are different:
$left.aggregateId              $right.aggregateId
"""
    }

}
