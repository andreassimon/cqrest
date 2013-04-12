package de.oneos.eventstore.springjdbc

import de.oneos.eventsourcing.Event

class CombinedEventClassResolver implements EventClassResolver {

    protected List<EventClassResolver> resolvers = []

    CombinedEventClassResolver(EventClassResolver... subResolvers) {
        assert subResolvers != null

        subResolvers.each { addResolver(it) }
    }

    void addResolver(EventClassResolver resolver) {
        assert resolver != null

        resolvers << resolver
    }


    @Override
    Class<? extends Event> resolveEvent(String eventName) {
        resolvers.findResult {
            try {
                return it.resolveEvent(eventName)
            } catch(ClassNotFoundException e) {
                return null
            }
        }
    }

}
