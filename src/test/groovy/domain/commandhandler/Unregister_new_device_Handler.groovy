package domain.commandhandler

import domain.commands.UnknownDeviceException
import domain.commands.Unregister_new_device
import domain.aggregates.Device

class Unregister_new_device_Handler extends CommandHandler {

    Unregister_new_device_Handler(repository, eventPublisher) {
        super(repository, eventPublisher)
    }

    void handle(Unregister_new_device command) {
        def deviceHistory = repository.getEventsFor('Device', command.deviceId)
        Device device = deviceHistory.inject null, { device, event ->
            event.applyTo device
        }
        if(!device) { throw new UnknownDeviceException() }

        device.handle(command, eventPublisher)
    }

    private Device deviceWith(UUID deviceId) {
        Device device = repository.getDevice(deviceId)
        return device ? device : new Device(deviceId)
    }
}
