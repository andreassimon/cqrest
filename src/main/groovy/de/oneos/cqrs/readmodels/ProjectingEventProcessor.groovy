package de.oneos.cqrs.readmodels

class ProjectingEventProcessor implements EventProcessor {

    Models readModels
    List projections = []

    def subscribeForEventsAt(EventSupplier eventSupplier) {
        projections.each {
            eventSupplier.subscribeTo(it.eventFilter, this)
        }
    }

    def add(Projection projection) {
        projections.add(projection)
    }

    void process(event) {
        projections.findAll {
            it.isApplicableTo(event)
        }.each {
            it.applyTo(readModels, event)
        }
    }
}
