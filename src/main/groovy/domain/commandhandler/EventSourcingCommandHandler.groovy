package domain.commandhandler

import infrastructure.persistence.DefaultEventStore

abstract class EventSourcingCommandHandler<C> {
    DefaultEventStore eventStore
    def delegateEventPublisher
    UnitOfWork unitOfWork

    EventSourcingCommandHandler(eventStore, eventPublisher) {
        this.eventStore = eventStore
        this.delegateEventPublisher = eventPublisher
    }

    void handleInUnitOfWork(C command) {
        unitOfWork = new UnitOfWork(delegateEventPublisher)

        handle(command)

        unitOfWork.flush()
    }

    abstract void handle(C command)
}
