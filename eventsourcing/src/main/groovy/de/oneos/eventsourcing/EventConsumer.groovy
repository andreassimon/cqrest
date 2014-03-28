package de.oneos.eventsourcing

interface EventConsumer {

    // TODO Remove from interface in favor of reactive approach
    @Deprecated
    void process(EventEnvelope eventEnvelope) throws EventProcessingException

    /**
     * This might be useful for initial filling of read models upon creation/registration.
     * This is also sufficient when using a reactive approach
     * @param eventStore
     */
    // TODO Rename
    void wasRegisteredAt(EventSupplier eventSupplier)

}
