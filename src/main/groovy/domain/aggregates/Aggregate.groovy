package domain.aggregates

import de.oneos.cqrs.eventstore.UnitOfWork
import domain.events.Event

class Aggregate {

    UnitOfWork unitOfWork

    protected void publishEvent(Event event) {
        this.unitOfWork.publish(event)
    }

}
