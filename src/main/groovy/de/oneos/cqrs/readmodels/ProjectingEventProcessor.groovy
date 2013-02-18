package de.oneos.cqrs.readmodels

class ProjectingEventProcessor implements EventProcessor {

    List projections = []

    def subscribeForEventsAt(EventSupplier eventDeliveryAdapter) {
        projections.each {
            eventDeliveryAdapter.subscribeTo(it.eventFilter, this)
        }
    }

    def add(Projection projection) {
        projections << projection
    }

}
