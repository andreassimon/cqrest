package de.oneos.eventsourcing

import groovy.transform.Canonical
import org.apache.commons.logging.*

import java.util.concurrent.*

import static java.util.concurrent.TimeUnit.MILLISECONDS

class Correlation {
    static Log log = LogFactory.getLog(Correlation)
    UUID id

    List<CorrelatedEvent> triggeredEvents = new CopyOnWriteArrayList<>()
    CountDownLatch latch

    Correlation(UUID id) {
        this.id = id
    }

    void consumeTriggeredEvent(String eventType) {
        synchronized(latch) {
            triggeredEvents << CorrelatedEvent.create(eventType)
            latch.countDown()
        }
    }

    def waitFor(List<String> events, Closure onSuccess, Closure onTimeout = {}) {
        latch = new CountDownLatch(events.size())

        if(latch.await(100, MILLISECONDS)) {
            return onSuccess()
        } else {
            return onTimeout()
        }
    }
}

@Canonical
class CorrelatedEvent {

    final String eventType

    static CorrelatedEvent create(String eventType) {
        return new CorrelatedEvent(eventType)
    }
}
