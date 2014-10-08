package org.cqrest.eventstore

import org.cqrest.eventsourcing.*


class EventCollisionOccurred extends EventStoreException {

    EventCollisionOccurred(EventEnvelope conflictingEnvelope) {
        super(message(conflictingEnvelope))
    }

    EventCollisionOccurred(EventEnvelope conflictingEnvelope, Throwable cause) {
        super(
            message(conflictingEnvelope),
            cause
        )
    }

    protected static message(EventEnvelope envelope) {
        "Event ['${envelope.applicationName}'.'${envelope.boundedContextName}'.'${envelope.aggregateName}'[${envelope.aggregateId}] #${envelope.sequenceNumber}] already exists. Dropping <$envelope.eventName[$envelope.eventAttributes]>"
    }

}
