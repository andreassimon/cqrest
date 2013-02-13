package domain.aggregates

import domain.events.Device_was_locked_out
import domain.events.Device_was_unregistered

import domain.events.Event

class Device extends Aggregate {

    UUID deviceId

    void lockOut(UUID deviceId) {
        publishEvent(new Device_was_locked_out(deviceId))
    }

    void unregister(UUID deviceId) {
        publishEvent(new Device_was_unregistered(deviceId: deviceId))
    }

    void apply(List<Event<Device>> events) {
        for(Event<Device> event: events) {
            event.applyTo(this)
        }
    }

}
