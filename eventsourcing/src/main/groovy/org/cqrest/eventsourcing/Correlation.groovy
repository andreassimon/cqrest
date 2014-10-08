package org.cqrest.eventsourcing

import org.apache.commons.logging.*

import java.util.concurrent.*

import static java.util.concurrent.TimeUnit.MILLISECONDS

// TODO Holding references to Correlations for e.g. messaging purposes might cause a memory leak,
//      Should be tested
class Correlation {
    static Log log = LogFactory.getLog(Correlation)

    UUID id

    final List<CorrelatedEvent> triggeredEvents = new CopyOnWriteArrayList<>()
    CountDownLatch latch

    Correlation(UUID id) {
        this.id = id
    }

    void consumeTriggeredEvent(String eventType) {
        synchronized(triggeredEvents) {
            log.debug "$this: Consuming '$eventType'"
            triggeredEvents << CorrelatedEvent.create(eventType)
            latch?.countDown()
        }
    }

    def waitFor(List<String> events, Closure onSuccess, Closure onTimeout = {}) {
        synchronized(triggeredEvents) {
            latch = new CountDownLatch((events - triggeredEvents).size())
        }

        if(latch.await(100, MILLISECONDS)) {
            return onSuccess()
        } else {
            return onTimeout()
        }
    }

    String getRoutingKey() {
        id.toString()
    }

    @Override
    String toString() {
        "Correlation{$id}"
    }

}
