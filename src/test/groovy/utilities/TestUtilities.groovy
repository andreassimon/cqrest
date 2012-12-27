package utilities

import domain.aggregates.Device
import domain.events.Event

class InMemoryRepository {
    def history = []

    Device getDevice(deviceId) {
        return null
    }

    def getEventsFor(Class clazz, entityId) {
        return history
    }
}

class InMemoryEventPublisher {
    def receivedEvents = []

    void publish(Event<?> event) {
        receivedEvents << event
    }

}
