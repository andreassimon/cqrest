package de.oneos.eventsourcing


class EventProcessingException extends RuntimeException {
    EventProcessingException(String message) {
        super(message)
    }
}
