package de.oneos.eventstore


interface EventConsumer {

    void process(EventEnvelope eventEnvelope) throws EventProcessingException

    /**
     * This might be useful for initial filling of read models upon creation/registration.
     * @param eventStore
     */
    void wasRegisteredAt(EventSupplier eventSupplier)

    // TODO Add to interface
//    Map<String, ?> getEventCriteria() {

}
