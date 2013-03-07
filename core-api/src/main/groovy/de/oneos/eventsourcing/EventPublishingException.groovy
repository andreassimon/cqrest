package de.oneos.eventsourcing


class EventPublishingException extends RuntimeException {
    EventPublishingException(String message) {
        super(message)
    }
}
