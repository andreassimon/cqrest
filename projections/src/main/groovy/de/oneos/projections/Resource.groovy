package de.oneos.projections

import de.oneos.eventsourcing.EventEnvelope


class Resource<T> {
    UUID correlationId
    UUID aggregateId
    int version
    Date lastModified

    T body

    public Resource<T> apply(EventEnvelope event) {
        body.invokeMethod(event.eventName, event.eventAttributes)
        return new Resource<T>(
          correlationId: event.correlationId,
          aggregateId: aggregateId,
          version: event.sequenceNumber,
          lastModified: event.timestamp,
          body: body
        )
    }

    public <R> Resource<R> transform(Closure<R> function) {
        return new Resource<R>(
            correlationId: correlationId,
            aggregateId: aggregateId,
            version: version,
            lastModified: lastModified,
            body: function(body)
        )
    }

}
