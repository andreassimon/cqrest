package de.oneos.cqrs.eventstore

class StaleStateException extends EventStoreException {
    StaleStateException(Throwable cause) {
        super(cause)
    }
}
