package de.oneos.cqrs.eventstore.inmemory

import de.oneos.cqrs.eventstore.EventStore
import de.oneos.cqrs.eventstore.UnknownAggregate
import domain.events.EventEnvelope
import infrastructure.persistence.PersistentEventPublisher
import de.oneos.cqrs.eventstore.UnitOfWork

class InMemoryEventStore implements EventStore {
    def history = []

    @Override
    def save(EventEnvelope eventEnvelope) {
        throw new IllegalAccessError('Not implemented in InMemoryEventStore')
    }

    public <A> A getAggregate(String applicationName, String boundedContextName, String aggregateName, Class<A> aggregateClass, UUID aggregateId, String eventPackageName) throws UnknownAggregate {
        def aggregateEvents = getEventsFor(applicationName, boundedContextName, aggregateName, aggregateId, eventPackageName)
        if(aggregateEvents.empty) { throw new UnknownAggregate(aggregateClass, aggregateId) }

        UnitOfWork unitOfWork = new UnitOfWork(new PersistentEventPublisher(eventStore: this))
        unitOfWork.applicationName = applicationName
        unitOfWork.boundedContextName = boundedContextName
        unitOfWork.aggregateName = aggregateName
        unitOfWork.aggregateId = aggregateId

        def properties = Collections.synchronizedMap([:])

        aggregateClass.metaClass {
            setUnitOfWork = { _unitOfWork ->
                properties[System.identityHashCode(delegate) + "unitOfWork"] = _unitOfWork
            }
            getUnitOfWork = { ->
                properties[System.identityHashCode(delegate) + "unitOfWork"]
            }
            publishEvent = { event ->
                delegate.unitOfWork.publish(event)
            }
            flush = { ->
                delegate.unitOfWork.flush()
            }
        }

        def aggregateInstance = aggregateClass.newInstance()
        aggregateInstance.unitOfWork = unitOfWork

        return aggregateEvents.inject(aggregateInstance) { aggregate, event ->
            event.applyTo(aggregate)
        }
    }

    @Override
    List getEventsFor(String applicationName = '', String boundedContextName = '', String aggregateName, UUID aggregateId, String eventPackageName) {
        return history
    }

}

