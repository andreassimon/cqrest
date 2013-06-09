package de.oneos.eventselection.amqp

import de.oneos.eventstore.*

class IllegalAmqpEventCoordinate extends EventProcessingException {
    IllegalAmqpEventCoordinate(List<String> eventCoordinates) {
        super("Event coordinates must not include '.', but was $eventCoordinates")
    }
}
