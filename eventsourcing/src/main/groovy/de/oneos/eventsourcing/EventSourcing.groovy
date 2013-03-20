package de.oneos.eventsourcing

import static java.lang.System.identityHashCode
import static java.util.Collections.*


class EventSourcing {

    static Map<Integer, List> newEvents = Collections.synchronizedMap([:])

    static emit(aggregate, Event[] events) {
        if(!newEvents.containsKey(identityHashCode(aggregate))) {
            newEvents[identityHashCode(aggregate)] = []
        }
        events.each {
            newEvents[identityHashCode(aggregate)] << it
            it.applyTo(aggregate)
        }
        return aggregate
    }

    static List<Event> getNewEvents(aggregate) {
        newEvents[identityHashCode(aggregate)] ?: emptyList()
    }

    /**
     * This is necessary for several reasons:
     * <ol>
     *   <li>
     *       When the new events of the aggregate are persisted, they are not new any more,
     *       so they have to be removed.
     *   </li>
     *
     *   <li>
     *       Flushing the events allows the aggregate to be re-used in subsequent transactions.
     *   </li>
     *
     *   <li>
     *       When the entry in the {@code newEvents} Map are never removed, this might lead to a
     *       memory leak.
     *   </li>
     * </ol>
     */
    static void flushEvents(aggregate) {
        newEvents.remove(identityHashCode(aggregate))
    }

}

