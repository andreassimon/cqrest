package de.oneos.eventsourcing

class EventBusNotInitialized extends RuntimeException {

    EventBusNotInitialized() {
        super('EventBus is not initialized. Please set an instance of EventBus.')
    }

}
