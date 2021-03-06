package org.cqrest.eventsourcing.test

import static org.cqrest.eventsourcing.test.Util.*


class SequenceNumbersDiffer extends EventDiff {
    RecordedEvent left
    RecordedEvent right

    SequenceNumbersDiffer(RecordedEvent left, RecordedEvent right) {
        assert left != null
        assert right != null

        this.left = left
        this.right = right
    }

    @Override
    String toString() {
        """Sequence numbers are different:
${abbreviate(left.aggregateId)}    $left.sequenceNumber                                 $right.sequenceNumber
"""
    }

}
