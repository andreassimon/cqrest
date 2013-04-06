package de.oneos.eventsourcing

public interface Event<AT> {

    public String getEventName()
    public <C extends Collection> C serializedProperties()

}
