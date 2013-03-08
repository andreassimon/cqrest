package de.oneos.cqrs.readmodels

import org.apache.commons.logging.Log
import org.apache.commons.logging.LogFactory

class ProjectingEventProcessor implements EventProcessor {
    static Log log = LogFactory.getLog(ProjectingEventProcessor)

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
        int numberOfFunctionCalls = 0
        projections.findAll {
            it.isApplicableTo(event)
        }.each {
            it.applyTo(readModels, event)
            numberOfFunctionCalls++
        }
        log.debug "${this.toString()} applied $numberOfFunctionCalls functions to $event"
    }

    @Override
    String toString() {
        "${this.class.simpleName}<readModels = $readModels>"
    }
}
