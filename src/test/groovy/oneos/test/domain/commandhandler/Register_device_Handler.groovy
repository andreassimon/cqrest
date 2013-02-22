package oneos.test.domain.commandhandler

import oneos.test.domain.commands.Register_device
import oneos.test.domain.events.Device_was_registered

import static java.util.UUID.randomUUID
import oneos.test.domain.DeviceRepository
import oneos.test.domain.aggregates.Device
import domain.commandhandler.EventSourcingCommandHandler

class Register_device_Handler extends EventSourcingCommandHandler<Register_device> {

    DeviceRepository _repository

    DeviceRepository getRepository() {
        if(!_repository) {
            _repository = new DeviceRepository(eventStore)
        }
        return _repository
    }

    void handle(Register_device command) {
        def generatedDeviceId = randomUUID()
        repository.assertDeviceDoesNotExist(generatedDeviceId)

        publishEvent(Device, generatedDeviceId, new Device_was_registered(
            deviceId: generatedDeviceId,
            deviceName: command.deviceName
        ))
    }

}
