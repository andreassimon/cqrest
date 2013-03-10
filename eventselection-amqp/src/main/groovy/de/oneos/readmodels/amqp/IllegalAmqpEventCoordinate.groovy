package de.oneos.eventselection.amqp

import de.oneos.eventstore.EventPublishingException


class IllegalAmqpEventCoordinate extends EventPublishingException {
    IllegalAmqpEventCoordinate(List<String> eventCoordinates) {
        super("Event coordinates must not include '.', but was $eventCoordinates")
    }
}
