import model.Device
import model.events.Event

class InMemoryRepository {
    Device getDevice(deviceId) {
        return null
    }

    def getEventsFor(Class clazz, UUID entityId) {
        return []
    }
}

class InMemoryEventPublisher {
    def receivedEvents = []

    void publish(Event<?> event) {
        receivedEvents << event
    }

}
