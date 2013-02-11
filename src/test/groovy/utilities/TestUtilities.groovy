package utilities

import domain.events.Event
import infrastructure.persistence.EventStore
import domain.events.EventEnvelope

class InMemoryEventStore implements EventStore {
    def history = []

    @Override
    def save(EventEnvelope eventEnvelope) {
        throw new IllegalAccessError('Not implemented in InMemoryEventStore')
    }

    def getAggregate(Class aggregateClass, UUID deviceId) {
        def aggregate = aggregateClass.newInstance()

        aggregate.apply(getEventsFor(aggregateClass, deviceId))

        return aggregate
    }

    @Override
    def getEventsFor(String applicationName = '', String boundedContextName = '', String aggregateName, UUID aggregateId) {
        return history
    }

}

class InMemoryEventPublisher {
    def receivedEvents = []

    void publish(Event<?> event) {
        receivedEvents << event
    }

}
