package de.oneos.eventstore

import de.oneos.eventsourcing.*


class UnitOfWork {

    List<EventEnvelope> publishedEventEnvelopes = []
    int nextSequenceNumber = 0

    void publishEvent(String applicationName, String boundedContextName, String aggregateName, UUID aggregateId, Event event) {
        publishedEventEnvelopes << new EventEnvelope(
            applicationName,
            boundedContextName,
            aggregateName,
            aggregateId,
            event,
            nextSequenceNumber
        )
        nextSequenceNumber++
    }

    def eachEventEnvelope(Closure callback) {
        publishedEventEnvelopes.each { callback(it) }
    }

}
