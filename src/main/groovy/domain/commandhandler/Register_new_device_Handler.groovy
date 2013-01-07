package domain.commandhandler

import domain.aggregates.Device
import domain.commands.Register_new_device
import domain.events.Event

class Register_new_device_Handler extends CommandHandler {

    Register_new_device_Handler(repository, eventPublisher) {
        super(repository, eventPublisher)
    }

    void handle(Register_new_device command) {
        Device device = new Device()

        device.apply(repository.getEventsFor(Device.class, command.deviceId))

        device.handle(command, eventPublisher)
    }

}
