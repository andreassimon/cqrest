package de.oneos.projections

import org.apache.commons.logging.Log
import org.apache.commons.logging.LogFactory

import de.oneos.eventsourcing.EventEnvelope


class Resource<T> {
    public static Log log = LogFactory.getLog(this)

    UUID correlationId
    UUID aggregateId
    int version
    Date lastModified

    T body

    public Resource<T> apply(EventEnvelope event) {
        if(!body.respondsTo(event.eventName, [Map] as Class[])) {
            log.warn("${body.getClass().getCanonicalName()} cannot handle event `$event.eventName`")
        } else {
            body.invokeMethod(event.eventName, event.eventAttributes)
        }

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
