package domain.commandhandler

import domain.aggregates.Device
import domain.commands.Unregister_device

import domain.DeviceRepository

class Unregister_device_Handler extends EventSourcingCommandHandler<Unregister_device> {

    DeviceRepository repository

    DeviceRepository getRepository() {
        if (repository) {
            return repository
        }
        repository = new DeviceRepository(eventStore)
        return repository
    }

    void handle(Unregister_device command) {
        Device device = getRepository().getDevice(command.deviceId)
        device.unregister(command.deviceId, delegateEventPublisher)
    }

}
