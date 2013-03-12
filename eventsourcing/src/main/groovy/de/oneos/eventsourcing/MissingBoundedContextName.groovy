package de.oneos.eventsourcing

class MissingBoundedContextName extends IllegalArgumentException {
    MissingBoundedContextName(Class aggregateClass) {
        super("$aggregateClass must have static attribute 'boundedContextName'")
    }
}
