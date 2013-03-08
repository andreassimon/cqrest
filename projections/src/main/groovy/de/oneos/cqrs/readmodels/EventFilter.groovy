package de.oneos.cqrs.readmodels

interface EventFilter {

    boolean matches(event)

    def withConstrainedValues(List<String> constrainedValues, Closure callback)

}
