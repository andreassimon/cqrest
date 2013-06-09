package de.oneos.projections

import org.apache.commons.logging.*

import de.oneos.eventselection.*
import de.oneos.readmodels.*
import de.oneos.eventstore.*


class ProjectingEventProcessor implements EventPublisher {
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

    void publish(EventEnvelope eventEnvelope) throws EventPublishingException {
        int numberOfFunctionCalls = 0
        projections.findAll {
            it.isApplicableTo(eventEnvelope)
        }.each {
            it.applyTo(readmodels, eventEnvelope)
            numberOfFunctionCalls++
        }
        log.debug "${this.toString()} applied $numberOfFunctionCalls projections to $eventEnvelope"
    }

    @Override
    String toString() {
        "${this.class.simpleName}<readModels = $readmodels>"
    }
}
