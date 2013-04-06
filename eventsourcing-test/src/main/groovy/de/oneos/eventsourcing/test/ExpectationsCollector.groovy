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

        List fillingEvents = []
        def sizeDiff = actualEvents.size() - expectedEvents.size()
        if(sizeDiff > 0) {
            fillingEvents = Collections.nCopies(sizeDiff, null)
        }

        List filledExpectedEvents = expectedEvents + fillingEvents

        [filledExpectedEvents, actualEvents].transpose().each { expected, actual ->
            if(expected != actual)
                throw new AssertionError("Expected events weren't emitted:\n" + diffEventStreams(filledExpectedEvents, actualEvents))
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
