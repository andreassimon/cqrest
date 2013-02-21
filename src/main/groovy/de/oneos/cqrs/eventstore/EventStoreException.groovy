package de.oneos.cqrs.eventstore

class EventStoreException extends RuntimeException {
    EventStoreException(Throwable throwable) {
        super(throwable)
    }
}
