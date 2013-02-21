package de.oneos.cqrs.readmodels.amqp

import framework.PublishingException

class IllegalAmqpEventCoordinate extends PublishingException {
    IllegalAmqpEventCoordinate(List<String> eventCoordinates) {
        super("Event coordinates must not include '.', but was $eventCoordinates")
    }
}
