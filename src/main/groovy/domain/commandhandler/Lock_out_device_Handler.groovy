package domain.commandhandler

import domain.aggregates.Device
import domain.commands.UnknownDeviceException

class Lock_out_device_Handler extends CommandHandler {
    Lock_out_device_Handler(repository, eventPublisher) {
        super(repository, eventPublisher)
    }

    def handle(command) {

        def deviceHistory = repository.getEventsFor(Device.class, command.deviceId)
        Device device = deviceHistory.inject null, { device, event ->
            event.applyTo device
        }
        if(!device) { throw new UnknownDeviceException() }

        device.handle(command, eventPublisher)
    }

}
