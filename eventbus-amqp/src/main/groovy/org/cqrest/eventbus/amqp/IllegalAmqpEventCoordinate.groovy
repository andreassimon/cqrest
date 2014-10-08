package org.cqrest.eventbus.amqp

import org.cqrest.eventsourcing.EventProcessingException


class IllegalAmqpEventCoordinate extends EventProcessingException {
    IllegalAmqpEventCoordinate(List<String> eventCoordinates) {
        super("Event coordinates must not include '.', but was $eventCoordinates")
    }
}
