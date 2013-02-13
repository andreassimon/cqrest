package domain.commandhandler

import infrastructure.persistence.EventStore
import framework.EventPublisher
import domain.events.Event

abstract class EventSourcingCommandHandler<C> {
    EventStore eventStore
    private EventPublisher eventPublisher
    private UnitOfWork unitOfWork

    void setEventPublisher(EventPublisher eventPublisher) {
        this.eventPublisher = eventPublisher
    }

    EventPublisher getDelegateEventPublisher() {
        return eventPublisher
    }

    void handleInUnitOfWork(C command) {
        unitOfWork = new UnitOfWork(delegateEventPublisher)

        handle(command)

        unitOfWork.flush()
    }

    abstract void handle(C command)

    protected void publishEvent(Class aggregateClass, UUID aggregateId, Event event) {
        unitOfWork.append(aggregateClass, aggregateId, event)
    }

    protected void collectEventsFrom(aggregate, Closure closure) {
        unitOfWork.append(aggregate, closure)
    }
}
