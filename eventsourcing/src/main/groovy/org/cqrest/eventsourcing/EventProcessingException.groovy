package org.cqrest.eventsourcing


class EventProcessingException extends RuntimeException {
    EventProcessingException(String message) {
        super(message)
    }
}
