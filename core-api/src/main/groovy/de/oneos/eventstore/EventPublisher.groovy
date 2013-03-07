package de.oneos.eventstore

import de.oneos.eventsourcing.EventEnvelope


interface EventPublisher {

    void publish(EventEnvelope eventEnvelope) throws EventPublishingException

}
