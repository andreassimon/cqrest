package de.oneos.eventstore


interface EventProcessor {

    void process(EventEnvelope eventEnvelope) throws EventProcessingException

    /**
     * This might be useful for initial filling of read models upon creation/registration.
     * @param eventStore
     */
    void wasRegisteredAt(EventStore eventStore)

}
