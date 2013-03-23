package de.oneos.eventstore.springjdbc

import de.oneos.eventsourcing.Event


interface EventClassResolver {

    Class<? extends Event> resolveEvent(String eventName)

}
