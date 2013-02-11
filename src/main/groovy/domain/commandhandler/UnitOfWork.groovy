package domain.commandhandler

import framework.EventPublisher
import domain.events.Event

class UnitOfWork implements EventPublisher {

    EventPublisher delegateEventPublisher
    List<Event> collectedEvents = new LinkedList<Event>()

    UnitOfWork(EventPublisher delegateEventPublisher) {
        this.delegateEventPublisher = delegateEventPublisher
    }

    @Override
    void publish(Event event) {
        collectedEvents.add(event)
    }

    void flush() {
        for(Event event in collectedEvents) {
            delegateEventPublisher.publish(event)
        }
    }

}
