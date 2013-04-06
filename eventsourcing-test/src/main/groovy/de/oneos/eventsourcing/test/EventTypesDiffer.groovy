package de.oneos.eventsourcing.test

import static de.oneos.eventsourcing.test.Util.*


class EventTypesDiffer extends EventDiff {
    RecordedEvent left
    RecordedEvent right

    EventTypesDiffer(RecordedEvent left, RecordedEvent right) {
        this.left = left
        this.right = right
    }

    @Override
    String toString() {
        StringBuilder builder = new StringBuilder()
        builder.append(abbreviate(aggregateId))
        builder.append('    ')
        builder.append(String.format('%-30s', leftEventName))
        builder.append('    ')
        builder.append(String.format('%-30s', rightEventName))
        builder.append('\n')
        builder.toString()
    }

    protected getAggregateId() {
        left?.aggregateId ?: right?.aggregateId
    }

    protected getRightEventName() {
        if(right == null) {
            return '<null>'
        }
        return right.event.eventName
    }

    protected getLeftEventName() {
        if (left == null) {
            return '<null>'
        }
        return left.event.eventName
    }
}
