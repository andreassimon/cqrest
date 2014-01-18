package de.oneos.eventsourcing

class MissingAggregateName extends IllegalArgumentException {
    MissingAggregateName(Class aggregateClass) {
        super("$aggregateClass must have static method `getAggregateName`")
    }
}
