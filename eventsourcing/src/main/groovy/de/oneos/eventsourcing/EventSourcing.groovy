package de.oneos.eventsourcing

import static java.lang.System.identityHashCode


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
    }

    static List<Event> getNewEvents(aggregate) {
        newEvents[identityHashCode(aggregate)]
    }

}

