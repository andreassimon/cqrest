package domain.commandhandler

import domain.commands.UnknownDeviceException
import domain.aggregates.Device

class Lock_out_device_Handler extends CommandHandler {
    Lock_out_device_Handler(repository, eventPublisher) {
        super(repository, eventPublisher)
    }

    def handle(command) {

        def deviceHistory = repository.getEventsFor('Device', command.deviceId)
        Device device = deviceHistory.inject null, { device, event ->
            event.applyTo device
        }
        if(!device) { throw new UnknownDeviceException() }

        device.handle(command, eventPublisher)
    }

}
