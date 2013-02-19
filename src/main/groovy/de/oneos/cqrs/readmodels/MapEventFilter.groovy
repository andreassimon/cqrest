package de.oneos.cqrs.readmodels

class MapEventFilter implements EventFilter {
    Map<String, String> eventConstraints

    MapEventFilter(Map<String, String> eventConstraints) {
        this.eventConstraints = eventConstraints
    }

    @Override
    String toString() {
        "${simpleClassName}<${eventConstraintsDescription}>"
    }

    private getEventConstraintsDescription() {
        eventConstraints.collect {attributeName, constrainedValue -> "$attributeName=='$constrainedValue'"}.join(' && ')
    }

    private getSimpleClassName() {
        this.class.simpleName
    }

    @Override
    boolean equals(Object that) {
        this.eventConstraints == that.eventConstraints
    }

    @Override
    boolean matches(event) {
        if(event.applicationName != this.applicationName) { return false }
        if(event.boundedContextName != this.boundedContextName) { return false }
        if(event.aggregateName != this.aggregateName) { return false }
        if(event.eventName != this.eventName) { return false }
        return true
    }

    private String getApplicationName() { eventConstraints.applicationName }
    private String getBoundedContextName() { eventConstraints.boundedContextName }
    private String getAggregateName() { eventConstraints.aggregateName }
    private String getEventName() { eventConstraints.eventName }
}
