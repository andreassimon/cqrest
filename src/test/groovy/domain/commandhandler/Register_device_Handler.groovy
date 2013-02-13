package domain.commandhandler

import domain.commands.Register_device
import domain.DeviceRepository
import domain.aggregates.Device
import domain.events.Device_was_registered

import static java.util.UUID.randomUUID

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
        def generatedDeviceId = randomUUID()
        getRepository().assertDeviceDoesNotExist(generatedDeviceId)

        publishEvent(Device, generatedDeviceId, new Device_was_registered(
            deviceId: generatedDeviceId,
            deviceName: command.deviceName
        ))
    }

}
