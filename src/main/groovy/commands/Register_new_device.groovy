package commands

import model.Device
import model.events.Event


class Register_new_device {
    UUID deviceId
    String deviceName
}


class Register_new_device_Handler extends CommandHandler {

    Register_new_device_Handler(repository, eventPublisher) {
        super(repository, eventPublisher)
    }

    void handle(Register_new_device command) {
        Device device = deviceWith(command.deviceId)

        device.apply(
                repository.getEventsFor(Device.class, command.deviceId))

        device.handle(command, eventPublisher)
    }

    private Device deviceWith(UUID deviceId) {
        Device device = repository.getDevice(deviceId)
        return device ? device : new Device(deviceId)
    }
}
