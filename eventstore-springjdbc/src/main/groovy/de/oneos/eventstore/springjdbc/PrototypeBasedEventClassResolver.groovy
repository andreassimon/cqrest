package de.oneos.eventstore.springjdbc

import de.oneos.eventsourcing.Event

class PrototypeBasedEventClassResolver implements EventClassResolver {

    Class<? extends Event> exemplaryEventClass

    PrototypeBasedEventClassResolver(Class<? extends Event> exemplaryEventClass) {
        assert exemplaryEventClass != null

        this.exemplaryEventClass = exemplaryEventClass
    }

    Class<Event> resolveEvent(String eventName) {
        String eventPackageName = exemplaryEventClass.package.name
        String fullEventClassName = fullyQualifiedEventClassName(eventPackageName, eventName)
        ClassLoader eventClassLoader = exemplaryEventClass.classLoader
        eventClassLoader.loadClass(fullEventClassName) as Class<Event>
    }

    protected static fullyQualifiedEventClassName(eventPackageName, eventName) {
        def simpleEventClassName = eventName.replaceAll(' ', '_')
        return [eventPackageName, simpleEventClassName].join('.')
    }

}
