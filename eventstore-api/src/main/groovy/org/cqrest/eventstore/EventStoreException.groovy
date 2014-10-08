package org.cqrest.eventstore


abstract class EventStoreException extends RuntimeException {

    EventStoreException(String message) {
        super(message)
    }

    EventStoreException(String message, Throwable cause) {
        super(message, cause)
    }

    @Override
    boolean equals(Object that) {
        this.getMessage() == that.getMessage()
    }

}
