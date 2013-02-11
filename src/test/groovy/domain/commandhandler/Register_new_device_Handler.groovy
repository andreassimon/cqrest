package domain.commandhandler

import domain.commands.Register_new_device
import domain.aggregates.Device

class Register_new_device_Handler extends CommandHandler {

    Register_new_device_Handler(repository, eventPublisher) {
        super(repository, eventPublisher)
    }

    void handle(Register_new_device command) {
        Device device = new Device()

        device.apply(repository.getEventsFor('Device', command.deviceId))

        device.handle(command, eventPublisher)
    }

}
