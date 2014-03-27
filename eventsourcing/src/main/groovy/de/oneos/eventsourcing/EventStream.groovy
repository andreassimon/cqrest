package de.oneos.eventsourcing

public interface EventStream {

    org.cqrest.reactive.Observable<EventEnvelope> observe(Map<String, ?> criteria)

}
