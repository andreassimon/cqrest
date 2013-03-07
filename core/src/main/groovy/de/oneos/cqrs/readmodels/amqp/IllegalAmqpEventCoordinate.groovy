package de.oneos.cqrs.readmodels.amqp

import de.oneos.eventsourcing.EventPublishingException


class IllegalAmqpEventCoordinate extends EventPublishingException {
    IllegalAmqpEventCoordinate(List<String> eventCoordinates) {
        super("Event coordinates must not include '.', but was $eventCoordinates")
    }
}
