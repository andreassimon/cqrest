package de.oneos.projections

import org.apache.commons.logging.*

import de.oneos.eventselection.*
import de.oneos.readmodels.*


class ProjectingEventProcessor implements EventProcessor {
    static Log log = LogFactory.getLog(ProjectingEventProcessor)

    Readmodels readmodels
    List<Projection> projections = []

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
            it.applyTo(readmodels, event)
            numberOfFunctionCalls++
        }
        log.debug "${this.toString()} applied $numberOfFunctionCalls projections to $event"
    }

    @Override
    String toString() {
        "${this.class.simpleName}<readModels = $readmodels>"
    }
}
