package domain.aggregates

import domain.events.Device_was_locked_out
import domain.events.Device_was_unregistered

import domain.commands.Lock_out_device
import domain.commands.Register_new_device
import domain.commands.Unregister_new_device
import domain.events.Event
import domain.events.New_device_was_registered

class Device {

    UUID deviceId


    Device(UUID deviceId) {
        // TODO Illegal: state may only be modified through events
        this.deviceId = deviceId
    }

    void handle(Register_new_device command, eventPublisher) throws DeviceAlreadyExists {
        if(deviceId) {
           throw new DeviceAlreadyExists(deviceId)
        }
        eventPublisher.publish(
            new New_device_was_registered(
                   deviceId: command.deviceId,
                   deviceName: command.deviceName
            )
        )
    }

    void handle(Lock_out_device command, eventPublisher) {
        eventPublisher.publish(new Device_was_locked_out(command.deviceId))
    }

    void handle(Unregister_new_device command, eventPublisher) {
        eventPublisher.publish(new Device_was_unregistered(deviceId: command.deviceId))
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

class DeviceAlreadyExists extends RuntimeException {

    final UUID deviceId

    DeviceAlreadyExists(UUID deviceId) {
        this.deviceId = deviceId
    }

}
