package model

import commands.Lock_out_device
import commands.Register_new_device
import model.events.Device_was_locked_out
import model.events.Event
import model.events.New_device_was_registered

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
        eventPublisher.publish(new New_device_was_registered(new Device.Id(command.deviceId), command.deviceName))
    }

    void handle(Lock_out_device command, eventPublisher) {
        eventPublisher.publish(new Device_was_locked_out(command.deviceId))
    }

    void apply(List<Event<Device>> events) {

    }

    // Why not move it to New_device_was_registered.applyTo(device)?
    void apply(New_device_was_registered event) {
        event.applyTo(this)
    }

}
