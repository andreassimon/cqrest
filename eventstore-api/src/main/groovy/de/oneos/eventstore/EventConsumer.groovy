package de.oneos.eventstore

import de.oneos.eventsourcing.*


interface EventConsumer {

    // TODO Remove from interface
    void process(EventEnvelope eventEnvelope) throws EventProcessingException

    /**
     * This might be useful for initial filling of read models upon creation/registration.
     * @param eventStore
     */
    void wasRegisteredAt(EventSupplier eventSupplier)

    Map<String, ?> getEventCriteria()

}
