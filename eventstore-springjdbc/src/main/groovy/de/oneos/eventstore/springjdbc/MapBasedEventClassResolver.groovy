package de.oneos.eventstore.springjdbc

import de.oneos.eventsourcing.Event


class MapBasedEventClassResolver implements EventClassResolver {

    Map<String, Class<? extends Event>> eventMap = [:]

    MapBasedEventClassResolver map(String eventName, Class<? extends Event> eventClass) {
        eventMap[eventName] = eventClass
    }

    Class<? extends Event> resolveEvent(String eventName) {
        eventMap[eventName]
    }

}
