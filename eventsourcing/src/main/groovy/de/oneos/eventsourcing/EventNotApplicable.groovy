package de.oneos.eventsourcing

class EventNotApplicable extends RuntimeException {
    EventNotApplicable(event, aggregate) {
        super("$event cannot be applied to $aggregate")
    }

}
