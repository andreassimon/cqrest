package de.oneos.eventstore


interface EventProcessor {

    void process(EventEnvelope eventEnvelope) throws EventProcessingException

}
