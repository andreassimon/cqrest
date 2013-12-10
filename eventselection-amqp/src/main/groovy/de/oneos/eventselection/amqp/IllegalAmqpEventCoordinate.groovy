package de.oneos.eventselection.amqp

import de.oneos.eventsourcing.EventProcessingException


class IllegalAmqpEventCoordinate extends EventProcessingException {
    IllegalAmqpEventCoordinate(List<String> eventCoordinates) {
        super("Event coordinates must not include '.', but was $eventCoordinates")
    }
}
