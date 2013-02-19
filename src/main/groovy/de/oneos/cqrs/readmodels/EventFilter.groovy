package de.oneos.cqrs.readmodels

interface EventFilter {

    boolean matches(event)

}
