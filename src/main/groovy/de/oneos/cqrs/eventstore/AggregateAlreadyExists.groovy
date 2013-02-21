package de.oneos.cqrs.eventstore

class AggregateAlreadyExists extends RuntimeException {
    AggregateAlreadyExists(Class aggregateClass, UUID aggregateId) {
        super("${aggregateClass.simpleName} with UUID $aggregateId already exists!")
    }
}
