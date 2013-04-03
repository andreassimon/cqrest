package de.oneos.eventsourcing.test

import static java.lang.String.*

class EventAttributesDiffer extends EventDiff {

    UUID aggregateId
    String eventName
    List<EventAttributeDiff> attributeDiffs

    EventAttributesDiffer(RecordedEvent left, RecordedEvent right) {
        assert left != null
        assert right != null
        assert left.event.eventName == right.event.eventName

        aggregateId = left.aggregateId
        eventName = left.event.eventName

        def leftAttributes = left.event.serializedProperties().sort { a, b -> a.key <=> b.key }
        def rightAttributes = right.event.serializedProperties().sort { a, b -> a.key <=> b.key }

        attributeDiffs = []
        de.oneos.eventsourcing.test.Util.pairwise(leftAttributes.iterator(), rightAttributes.iterator()) { leftAttribute, rightAttribute ->
            attributeDiffs << new EventAttributeDiff(leftAttribute, rightAttribute)
        }
    }

    @Override
    String toString() {
        StringBuilder builder = new StringBuilder()
        builder.append(format('%s    %-30s    =%n', de.oneos.eventsourcing.test.Util.abbreviate(aggregateId), eventName))
        String attributeDiffFormat = "                   %-${maxAttributeNameLength}s  %-${maxLeftValueLength}s    %s%n"
        attributeDiffs.each {
            builder.append(it.formatWith(attributeDiffFormat))
        }
        builder.toString()
    }

    protected int getMaxAttributeNameLength() {
        attributeDiffs.collect { it.attributeName.size() }.max()
    }

    protected int getMaxLeftValueLength() {
        attributeDiffs.collect { it.leftValue.toString().size() }.max()
    }

}