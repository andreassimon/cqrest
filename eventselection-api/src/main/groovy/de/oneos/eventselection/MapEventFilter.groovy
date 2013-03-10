package de.oneos.eventselection

class MapEventFilter implements EventFilter {
    Map<String, String> eventConstraints

    MapEventFilter(Map<String, String> eventConstraints = [:]) {
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
        if(this.doesNotMatchApplicationNameOf(event)) { return false }
        if(this.doesNotMatchBoundedContextNameOf(event)) { return false }
        if(this.doesNotMatchAggregateNameOf(event)) { return false }
        if(this.doesNotMatchEventNameOf(event)) { return false }
        return true
    }

    private doesNotMatchApplicationNameOf(event) { applicationNameIsConstrained() && event.applicationName != this.applicationName }
    private doesNotMatchBoundedContextNameOf(event) { boundedContextNameIsConstrained() && event.boundedContextName != this.boundedContextName }
    private doesNotMatchAggregateNameOf(event) { aggregateNameIsConstrained() && event.aggregateName != this.aggregateName }
    private doesNotMatchEventNameOf(event) { eventNameIsConstrained() && event.eventName != this.eventName }

    private Boolean applicationNameIsConstrained() { return this.applicationName }
    private Boolean boundedContextNameIsConstrained() { return this.boundedContextName }
    private Boolean aggregateNameIsConstrained() { return this.aggregateName }
    private Boolean eventNameIsConstrained() { return this.eventName }

    private String getApplicationName() { eventConstraints.applicationName }
    private String getBoundedContextName() { eventConstraints.boundedContextName }
    private String getAggregateName() { eventConstraints.aggregateName }
    private String getEventName() { eventConstraints.eventName }

    @Override
    public <R> R withConstrainedValues(List<String> constrainedValues, Closure<R> callback) {
        callback(constrainedValues.collect { this[it] })
    }
}
