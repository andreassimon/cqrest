package de.oneos.eventstore


class EventPublishingException extends RuntimeException {
    EventPublishingException(String message) {
        super(message)
    }
}
