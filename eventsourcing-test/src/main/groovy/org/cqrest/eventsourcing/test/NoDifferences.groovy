package org.cqrest.eventsourcing.test

import static java.lang.String.*

class NoDifferences extends EventDiff {

    RecordedEvent left, right

    NoDifferences(RecordedEvent left, RecordedEvent right) {
        this.left = left
        this.right = right
    }

    @Override
    String toString() {
        format('%s    %s    =%n', org.cqrest.eventsourcing.test.Util.abbreviate(left.aggregateId), left.eventName)
    }

}
