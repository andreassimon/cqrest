package org.cqrest.eventsourcing.test

import static java.lang.String.*

class EventAttributesDiffer extends EventDiff {

    UUID aggregateId
    String eventName
    List<EventAttributeDiff> attributeDiffs

    EventAttributesDiffer(RecordedEvent left, RecordedEvent right) {
        assert left != null
        assert right != null
        assert left.eventName == right.eventName

        aggregateId = left.aggregateId
        eventName = left.eventName

        def leftAttributes = left.eventAttributes.sort { a, b -> a.key.toLowerCase() <=> b.key.toLowerCase() }
        def rightAttributes = right.eventAttributes.sort { a, b -> a.key.toLowerCase() <=> b.key.toLowerCase() }

        def unitedAttributes = (leftAttributes + rightAttributes).collect { it.key }.unique()

        attributeDiffs = unitedAttributes.collect { attribute ->
            new EventAttributeDiff(attribute, leftAttributes[attribute], rightAttributes[attribute])
        }
    }

    @Override
    String toString() {
        StringBuilder builder = new StringBuilder()
        builder.append(format('%s    %-30s    =%n', org.cqrest.eventsourcing.test.Util.abbreviate(aggregateId), eventName))
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
