package de.oneos.eventstore.springjdbc

import de.oneos.eventsourcing.Event

class PrototypeBasedEventClassResolver implements EventClassResolver {

    Class<Event> examplaryEventClass

    PrototypeBasedEventClassResolver(Class<Event> exemplaryEventClass) {
        assert examplaryEventClass != null

        this.examplaryEventClass = examplaryEventClass
    }

    Class<Event> resolveEvent(String eventName) {
        String eventPackageName = examplaryEventClass.package.name
        String fullEventClassName = fullyQualifiedEventClassName(eventPackageName, eventName)
        ClassLoader eventClassLoader = examplaryEventClass.classLoader
        eventClassLoader.loadClass(fullEventClassName) as Class<Event>
    }

    protected static fullyQualifiedEventClassName(eventPackageName, eventName) {
        def simpleEventClassName = eventName.replaceAll(' ', '_')
        return [eventPackageName, simpleEventClassName].join('.')
    }

}
