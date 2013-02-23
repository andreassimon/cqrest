package de.oneos.eventsourcing

public interface EventPublisher {

    void publish(EventEnvelope eventEnvelope) throws EventPublishingException

}
