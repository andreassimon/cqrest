package de.oneos.eventstore

import de.oneos.eventsourcing.*


class UnitOfWork {

    EventEnvelope publishedEventEnvelope

    void publishEvent(String applicationName, String boundedContextName, String aggregateName, UUID aggregateId, Event event) {
        publishedEventEnvelope = new EventEnvelope(
            applicationName,
            boundedContextName,
            aggregateName,
            aggregateId,
            event
        )
    }

    def eachEventEnvelope(Closure callback) {
        callback(publishedEventEnvelope)
    }

}
