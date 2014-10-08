package org.cqrest.eventsourcing

public interface Event<AT> {

    public String getEventName()
    public Map<String, ?> getEventAttributes()

}
