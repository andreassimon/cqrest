package oneos.test.domain.commandhandler

import oneos.test.domain.commands.Lock_out_device
import oneos.test.domain.DeviceRepository
import oneos.test.domain.aggregates.Device
import domain.commandhandler.EventSourcingCommandHandler


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
            lockOut()
        }
    }

}
