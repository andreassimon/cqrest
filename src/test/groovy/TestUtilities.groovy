import model.Device
import model.events.Event

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
