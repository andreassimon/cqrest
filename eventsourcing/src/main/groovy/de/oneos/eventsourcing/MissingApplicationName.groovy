package de.oneos.eventsourcing

class MissingApplicationName extends IllegalArgumentException {
    MissingApplicationName(Class aggregateClass) {
        super("$aggregateClass must have static attribute 'applicationName'")
    }
}
