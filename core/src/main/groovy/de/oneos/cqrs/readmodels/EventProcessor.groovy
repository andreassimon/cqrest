package de.oneos.cqrs.readmodels

interface EventProcessor {

    void process(event)

}
