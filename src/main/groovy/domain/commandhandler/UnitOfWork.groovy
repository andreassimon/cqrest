package domain.commandhandler

import framework.EventPublisher
import domain.events.Event
import domain.events.EventEnvelope

class UnitOfWork {

    EventPublisher delegateEventPublisher
    List<EventEnvelope> collectedEventEnvelopes = new LinkedList<EventEnvelope>()
    String applicationName
    String boundedContextName
    String aggregateName
    UUID aggregateId

    UnitOfWork(EventPublisher delegateEventPublisher) {
        this.delegateEventPublisher = delegateEventPublisher
    }

    def append(aggregate) {
        aggregate
    }

    def append(Class aggregateClass, Event event) {
        publish(event)
    }

    def append(aggregate, Closure closure) {
        aggregate.unitOfWork = this
        closure.delegate = aggregate
        closure()
    }

    void publish(Event event) {
        collectedEventEnvelopes.add(
            new EventEnvelope(applicationName, boundedContextName, aggregateName, aggregateId, event)
        )
    }

    void flush() {
        for(EventEnvelope eventEnvelope in collectedEventEnvelopes) {
            delegateEventPublisher.publish(eventEnvelope)
        }
    }

}
