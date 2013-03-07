package de.oneos.eventstore


class EventCollisionOccurred extends RuntimeException {

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
        "Event ['${envelope.applicationName}'.'${envelope.boundedContextName}'.'${envelope.aggregateName}'[${envelope.aggregateId}] #${envelope.sequenceNumber}] already exists"
    }

}
