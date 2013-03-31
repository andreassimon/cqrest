package de.oneos.eventsourcing.test

class EventStreamDiff {

    List<EventDiff> eventDiffs

    EventStreamDiff(List<RecordedEvent> leftStream, List<RecordedEvent> rightStream) {
        eventDiffs = []

        de.oneos.eventsourcing.test.Util.pairwise(leftStream, rightStream) { RecordedEvent leftRecord, RecordedEvent rightRecord ->
            eventDiffs << EventDiff.of(leftRecord, rightRecord)
        }
    }

    @Override
    public String toString() {
        eventDiffs.collect { it.toString() }.join('\n')
    }


}
