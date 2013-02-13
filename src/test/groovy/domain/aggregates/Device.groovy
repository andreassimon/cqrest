package domain.aggregates

import domain.events.Device_was_locked_out
import domain.events.Device_was_unregistered

import domain.events.Event

class Device extends Aggregate {

    static String applicationName = 'CQRS Core Library'
    static String boundedContextName = 'Tests'
    static String aggregateName = 'Device'

    void lockOut() {
        publishEvent(new Device_was_locked_out())
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
