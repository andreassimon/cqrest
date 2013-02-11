package domain.commandhandler

import domain.aggregates.Device
import domain.commands.Lock_out_device
import domain.Repository

class Lock_out_device_Handler extends EventSourcingCommandHandler<Lock_out_device> {
    Repository repository

    Lock_out_device_Handler(repository, eventPublisher) {
        super(repository, eventPublisher)
        this.repository = new Repository(eventStore, 'CQRS Core Library', 'Test')
    }

    void handle(Lock_out_device command) {
        Device device = repository.getDevice(command.deviceId)

        device.lockOut(command.deviceId, delegateEventPublisher)
    }

}
