package commands

import model.Device

class Lock_out_device {
    final Device.Id deviceId

    Lock_out_device(Device.Id deviceId) {
        this.deviceId = deviceId
    }
}

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

class UnknownDeviceException extends RuntimeException {}
