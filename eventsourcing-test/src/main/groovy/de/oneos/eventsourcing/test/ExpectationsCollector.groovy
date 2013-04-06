package de.oneos.eventsourcing.test

import de.oneos.eventsourcing.*
import de.oneos.eventstore.*
import de.oneos.eventstore.inmemory.*

class ExpectationsCollector {

    InMemoryEventStore eventStore
    List<RecordedEvent> expectedEvents = []

    ExpectationsCollector(InMemoryEventStore eventStore) {
        this.eventStore = eventStore
    }

    void assertAreMet(Closure<?> expectations, int numberOfGivenEvents) {
        this.with(expectations)

        List<RecordedEvent> actualEvents

        if(eventStore.history.size() <= numberOfGivenEvents) {
            actualEvents = []
        } else {
            actualEvents = eventStore.history[numberOfGivenEvents..-1].collect { EventEnvelope it ->
                return new RecordedEvent(
                    aggregateId: it.aggregateId,
                    sequenceNumber: it.sequenceNumber,
                    event: it.event
                )
            }
        }

        List eventStreams = balanceStreamLengths(expectedEvents, actualEvents)

        eventStreams.transpose().each { expected, actual ->
            if(expected != actual)
                throw new AssertionError("Expected events weren't emitted:\n" + diffEventStreams(eventStreams))
        }
    }

    protected balanceStreamLengths(List<RecordedEvent> expectedEvents, List<RecordedEvent> actualEvents) {
        def sizeDiff = actualEvents.size() - expectedEvents.size()
        if (sizeDiff >= 0) {
            return  [expectedEvents + Collections.nCopies(sizeDiff, null), actualEvents]
        } else {
            return  [expectedEvents, actualEvents + Collections.nCopies(-sizeDiff, null)]
        }
    }

    String diffEventStreams(List<RecordedEvent> left, List<RecordedEvent> right) {
        new EventStreamDiff(left, right).toString()
    }

    void event(UUID aggregateId, int sequenceNumber, Event event) {
        expectedEvents << new RecordedEvent(
            aggregateId: aggregateId,
            sequenceNumber: sequenceNumber,
            event: event
        )
    }

}
