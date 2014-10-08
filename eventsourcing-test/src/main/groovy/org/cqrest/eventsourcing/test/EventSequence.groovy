package org.cqrest.eventsourcing.test

protected class EventSequence {

    Map<UUID, Integer> sequenceNumbers = [:]

    int next(UUID aggregateId) {
        if(!sequenceNumbers.containsKey(aggregateId)) {
            sequenceNumbers[aggregateId] = 0
        }
        sequenceNumbers[aggregateId]++
    }

}
