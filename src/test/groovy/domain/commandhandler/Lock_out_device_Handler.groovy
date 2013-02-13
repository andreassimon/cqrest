package domain.commandhandler

import domain.aggregates.Device
import domain.commands.Lock_out_device

import domain.DeviceRepository

class Lock_out_device_Handler extends EventSourcingCommandHandler<Lock_out_device> {
    DeviceRepository repository

    DeviceRepository getRepository() {
        if (repository) {
            return repository
        }
        repository = new DeviceRepository(eventStore)
        return repository
    }

    void handle(Lock_out_device command) {
        Device device = getRepository().getDevice(command.deviceId)

        collectEventsFrom(device) {
            lockOut(command.deviceId)
        }
    }

}
