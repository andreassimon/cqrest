package de.oneos.eventsourcing


class ClosureEventConsumer implements EventConsumer {

    private Map<String, ?> eventCriteria
    private Closure callback

    ClosureEventConsumer(Map<String, ?> eventCriteria, Closure callback) {
        this.callback = callback
        this.eventCriteria = eventCriteria
    }

    @Override
    void process(EventEnvelope eventEnvelope) throws EventProcessingException {
        callback(eventEnvelope)
    }

    @Override
    void wasRegisteredAt(EventSupplier eventSupplier) {
        // Ignore
    }

}
