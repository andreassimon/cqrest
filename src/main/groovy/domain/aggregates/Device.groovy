package domain.aggregates

import domain.commands.Lock_out_device
import domain.commands.Register_new_device
import domain.events.Device_was_locked_out
import domain.events.Event
import domain.events.New_device_was_registered

class Device {

    Id deviceId

    static class Id {
        UUID uuid;

        Id(UUID uuid = UUID.randomUUID()) {
            this.uuid = uuid
        }

        @Override
        String toString() {
            uuid.toString()
        }

        @Override
        boolean equals(Object that) {
            this.uuid == that.uuid
        }
    }

    Device(UUID deviceId) {
        this(new Id(deviceId))
    }

    Device(Id deviceId) {
        this.deviceId = deviceId
    }

    void handle(Register_new_device command, eventPublisher) {
        eventPublisher.publish(
            new New_device_was_registered(new Device.Id(command.deviceId), command.deviceName)
        )
    }

    void Register_new_device(deviceId, deviceName, eventPublisher) {
        eventPublisher.publish(
            new New_device_was_registered(new Device.Id(deviceId), deviceName)
        )
    }

    void handle(Lock_out_device command, eventPublisher) {
        eventPublisher.publish(new Device_was_locked_out(command.deviceId))
    }

    void apply(List<Event<Device>> events) {
        for(Event<Device> event: events) {
            apply(event)
        }
    }

    // Why not move it to New_device_was_registered.applyTo(device)?
    void apply(New_device_was_registered event) {
        event.applyTo(this)
    }

}
