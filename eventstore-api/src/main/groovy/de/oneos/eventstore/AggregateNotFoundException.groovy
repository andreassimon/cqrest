package de.oneos.eventstore

class AggregateNotFoundException extends RuntimeException {
    AggregateNotFoundException(Class aggregateClass, UUID id) {
        super("No ${aggregateClass.simpleName} with id <$id> exists")
    }
}
