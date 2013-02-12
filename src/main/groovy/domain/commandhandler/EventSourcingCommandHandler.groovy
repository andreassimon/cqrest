package domain.commandhandler

import infrastructure.persistence.EventStore
import framework.EventPublisher

abstract class EventSourcingCommandHandler<C> {
    EventStore eventStore
    EventPublisher eventPublisher
    UnitOfWork unitOfWork

    EventPublisher getDelegateEventPublisher() {
        return eventPublisher
    }

    void handleInUnitOfWork(C command) {
        unitOfWork = new UnitOfWork(delegateEventPublisher)

        handle(command)

        unitOfWork.flush()
    }

    abstract void handle(C command)
}
