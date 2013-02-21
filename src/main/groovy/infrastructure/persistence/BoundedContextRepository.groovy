package infrastructure.persistence

import de.oneos.cqrs.eventstore.AggregateAlreadyExists
import de.oneos.cqrs.eventstore.EventStore
import de.oneos.cqrs.eventstore.UnknownAggregate

abstract class BoundedContextRepository {

    private EventStore eventStore
    String applicationName
    String boundedContextName

    BoundedContextRepository(EventStore eventStore, String applicationName, String boundedContextName) {
        this.eventStore = eventStore
        this.applicationName = applicationName
        this.boundedContextName = boundedContextName
    }

    protected void assertAggregateDoesNotExist(String aggregateName, Class aggregateClass, UUID aggregateId) throws AggregateAlreadyExists {
        try {
            if (getAggregate(aggregateName, aggregateClass, aggregateId)) {
                throw new AggregateAlreadyExists(aggregateClass, aggregateId)
            }
        } catch (UnknownAggregate e) {
            // This is expected
        }
    }

    protected getAggregate(String aggregateName, Class aggregateClass, UUID aggregateId) throws UnknownAggregate {
        eventStore.getAggregate(applicationName, boundedContextName, aggregateName, aggregateClass, aggregateId, 'domain.events.')
    }

}
