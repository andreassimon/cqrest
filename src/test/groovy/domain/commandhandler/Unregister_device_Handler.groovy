package domain.commandhandler

import domain.aggregates.Device
import domain.commands.Unregister_device
import domain.Repository

class Unregister_device_Handler extends EventSourcingCommandHandler<Unregister_device> {

    Repository repository

    Unregister_device_Handler(repository, eventPublisher) {
        super(repository, eventPublisher)
        this.repository = new Repository(eventStore, 'CQRS Core Library', 'Test')
    }

    void handle(Unregister_device command) {
        Device device = repository.getDevice(command.deviceId)
        device.unregister(command.deviceId, delegateEventPublisher)
    }

}
