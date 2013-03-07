package de.oneos.eventsourcing

import de.oneos.eventsourcing.*


public interface EventAggregator {

    void publishEvent(String applicationName, String boundedContextName, String aggregateName, UUID aggregateId, Event event)

}
