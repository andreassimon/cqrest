package utilities

import de.oneos.cqrs.eventstore.EventStore
import de.oneos.cqrs.eventstore.UnknownAggregate
import domain.events.EventEnvelope

class InMemoryEventStore implements EventStore {
    def history = []

    @Override
    def save(EventEnvelope eventEnvelope) {
        throw new IllegalAccessError('Not implemented in InMemoryEventStore')
    }

    public <A> A getAggregate(String applicationName, String boundedContextName, String aggregateName, Class<A> aggregateClass, UUID aggregateId, String eventPackageName) throws UnknownAggregate {
        def aggregateEvents = getEventsFor(applicationName, boundedContextName, aggregateName, aggregateId, eventPackageName)
        if(aggregateEvents.empty) { throw new UnknownAggregate(aggregateClass, aggregateId) }

        return aggregateEvents.inject(aggregateClass.newInstance()) { aggregate, event ->
            event.applyTo(aggregate)
        }
    }

    @Override
    List getEventsFor(String applicationName = '', String boundedContextName = '', String aggregateName, UUID aggregateId, String eventPackageName) {
        return history
    }

}

