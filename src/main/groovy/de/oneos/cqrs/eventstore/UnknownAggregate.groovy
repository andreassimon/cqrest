package de.oneos.cqrs.eventstore

class UnknownAggregate extends RuntimeException {
    UnknownAggregate(Class aggregateClass, UUID aggregateId) {
        super("${aggregateClass.simpleName} with UUID $aggregateId does not exist!")
    }
}
