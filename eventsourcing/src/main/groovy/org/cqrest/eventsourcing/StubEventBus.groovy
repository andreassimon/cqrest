package org.cqrest.eventsourcing

import org.apache.commons.logging.Log
import org.apache.commons.logging.LogFactory


class StubEventBus implements EventBus {

    public static Log log = LogFactory.getLog(this)


    @Override
    Correlation subscribeCorrelation(Correlation correlation) {
        log.warn "Correlation `$correlation` is subscribed to $this. It won't be notified of events."
        return correlation
    }

    @Override
    void emit(UUID correlation, String eventType) {
        log.warn "Event `$eventType` is emitted to correlation `$correlation` using $this. It won't be populated."
    }

    @Override
    String toString() { 'StubEventBus' }

}
