package de.oneos.eventsourcing

public interface Event<AT> {

    public String getEventName()
    public def getSerializableForm()
    public <C extends Collection> C serializedProperties()
    public void applyTo(AT aggregate)

}
