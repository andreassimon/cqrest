package de.oneos.cqrs.readmodels

class ProjectingEventProcessor implements EventProcessor {

    List projections = []

    def subscribeForEventsAt(EventSupplier eventSupplier) {
        projections.each {
            eventSupplier.subscribeTo(it.eventFilter, this)
        }
    }

    def add(Projection projection) {
        projections << projection
    }

}
