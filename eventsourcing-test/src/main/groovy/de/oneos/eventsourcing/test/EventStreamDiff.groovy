package de.oneos.eventsourcing.test

class EventStreamDiff {

    List<EventDiff> eventDiffs

    EventStreamDiff(List<RecordedEvent> leftStream, List<RecordedEvent> rightStream) {
        eventDiffs = []

        [leftStream, rightStream].transpose().each { RecordedEvent leftRecord, RecordedEvent rightRecord ->
            eventDiffs << EventDiff.of(leftRecord, rightRecord)
        }
    }

    @Override
    public String toString() {
        eventDiffs.collect { it.toString() }.join('\n')
    }


}
