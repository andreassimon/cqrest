package oneos.test.domain.commandhandler

import oneos.test.domain.commands.Unregister_device
import oneos.test.domain.DeviceRepository
import domain.commandhandler.EventSourcingCommandHandler


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
        collectEventsFrom(getDevice(command.deviceId)) {
            unregister(command.deviceId)
        }
    }

    private Object getDevice(UUID deviceId) {
        getRepository().getDevice(deviceId)
    }

}
