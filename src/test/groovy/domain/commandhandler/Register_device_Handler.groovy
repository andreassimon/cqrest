package domain.commandhandler

import domain.events.New_device_was_registered
import domain.commands.Register_device
import domain.DeviceRepository

class Register_device_Handler extends EventSourcingCommandHandler<Register_device> {

    DeviceRepository repository

    DeviceRepository getRepository() {
        if (repository) {
            return repository
        }
        repository = new DeviceRepository(eventStore)
        return repository
    }

    void handle(Register_device command) {
        getRepository().assertDeviceDoesNotExist(command.deviceId)

        unitOfWork.publish(
            new New_device_was_registered(
                deviceId: command.deviceId,
                deviceName: command.deviceName
            )
        )
    }

}
