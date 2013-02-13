package domain.commandhandler

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
        collectEventsFrom(getDevice(command.deviceId)) {
            unregister(command.deviceId)
        }
    }

    private Object getDevice(UUID deviceId) {
        getRepository().getDevice(deviceId)
    }

}
