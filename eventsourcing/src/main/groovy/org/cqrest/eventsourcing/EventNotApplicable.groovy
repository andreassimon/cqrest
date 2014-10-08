package org.cqrest.eventsourcing

class EventNotApplicable extends RuntimeException {
    EventNotApplicable(event, aggregate, Exception cause) {
        super("$event cannot be applied to $aggregate", cause)
    }

}
