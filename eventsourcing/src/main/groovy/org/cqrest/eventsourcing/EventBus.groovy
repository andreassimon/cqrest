package org.cqrest.eventsourcing


interface EventBus {

    Correlation subscribeCorrelation(Correlation correlation)

    void emit(UUID correlation, String eventType)

}
