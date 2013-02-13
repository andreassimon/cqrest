package domain.aggregates

import domain.commandhandler.UnitOfWork
import domain.events.Event

class Aggregate {

    UnitOfWork unitOfWork

    protected void publishEvent(Event event) {
        this.unitOfWork.publish(event)
    }

}
