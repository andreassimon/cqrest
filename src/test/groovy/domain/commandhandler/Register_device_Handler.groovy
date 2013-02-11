package domain.commandhandler

import domain.Repository
import infrastructure.persistence.DefaultEventStore
import domain.events.New_device_was_registered
import domain.commands.Register_device

class Register_device_Handler extends EventSourcingCommandHandler<Register_device> {

    Repository repository

    Register_device_Handler(DefaultEventStore eventStore, eventPublisher) {
        super(eventStore, eventPublisher)
        repository = new Repository(eventStore, 'CQRS Core Library', 'Test')
    }

    void handle(Register_device command) {
        repository.assertDeviceDoesNotExist(command.deviceId)

        unitOfWork.publish(
            new New_device_was_registered(
                deviceId: command.deviceId,
                deviceName: command.deviceName
            )
        )
    }

}
