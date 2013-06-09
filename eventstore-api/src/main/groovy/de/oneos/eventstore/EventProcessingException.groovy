package de.oneos.eventstore


class EventProcessingException extends RuntimeException {
    EventProcessingException(String message) {
        super(message)
    }
}
