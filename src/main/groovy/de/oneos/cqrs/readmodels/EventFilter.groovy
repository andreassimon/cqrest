package de.oneos.cqrs.readmodels

interface EventFilter {

    boolean matches(event)

    String getApplicationName()
    String getBoundedContextName()
    String getAggregateName()
    String getEventName()

}
