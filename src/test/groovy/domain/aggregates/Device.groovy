package domain.aggregates

import domain.events.Device_was_locked_out
import domain.events.Device_was_unregistered

import domain.events.Event

class Device {

    UUID deviceId


    Device(UUID deviceId) {
        // TODO Illegal: state may only be modified through events
        this.deviceId = deviceId
    }

    void lockOut(UUID deviceId, def eventPublisher) {
        eventPublisher.publish(new Device_was_locked_out(deviceId))
    }

    void unregister(UUID deviceId, def eventPublisher) {
        eventPublisher.publish(new Device_was_unregistered(deviceId: deviceId))
    }

    void apply(List<Event<Device>> events) {
        for(Event<Device> event: events) {
            event.applyTo(this)
        }
    }

}
