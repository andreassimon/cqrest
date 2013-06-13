package de.oneos.projections

import org.apache.commons.logging.*

import de.oneos.readmodels.*
import de.oneos.eventstore.*


class ProjectingEventConsumer implements EventConsumer {
    static Log log = LogFactory.getLog(ProjectingEventConsumer)

    Readmodels readmodels
    List<Projection> projections = []

    def subscribeForEventsAt(EventSupplier eventSupplier) {
        projections.each {
            eventSupplier.subscribeTo(it.criteria, this)
        }
    }

    def add(Projection projection) {
        projections.add(projection)
    }

    void process(EventEnvelope eventEnvelope) throws EventProcessingException {
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
    void wasRegisteredAt(EventSupplier eventSupplier) {
        // TODO implement
        throw new RuntimeException("Not implemented")
    }

    @Override
    String toString() {
        "${this.class.simpleName}<readModels = $readmodels>"
    }
}
