package de.oneos.eventselection

interface EventFilter {

    boolean matches(event)

    public <R> R withConstrainedValues(List<String> constrainedValues, Closure<R> callback)

}
