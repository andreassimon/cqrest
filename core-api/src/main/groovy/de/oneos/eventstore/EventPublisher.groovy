package de.oneos.eventstore


interface EventPublisher {

    void publish(EventEnvelope eventEnvelope) throws EventPublishingException

}
