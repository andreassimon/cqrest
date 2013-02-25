package de.oneos.eventstore

import de.oneos.eventsourcing.EventEnvelope

class EventCollisionOccurred extends RuntimeException {

    EventCollisionOccurred(EventEnvelope envelope, Throwable cause) {
        super(
            "Event ['${envelope.applicationName}'.'${envelope.boundedContextName}'.'${envelope.aggregateName}'[${envelope.aggregateId}] #${envelope.sequenceNumber}] already exists",
            cause
        )
    }

}
