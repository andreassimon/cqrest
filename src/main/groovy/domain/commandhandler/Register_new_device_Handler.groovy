package domain.commandhandler

import domain.aggregates.Device
import domain.commands.Register_new_device
import domain.events.Event

class Register_new_device_Handler extends CommandHandler {

    Register_new_device_Handler(repository, eventPublisher) {
        super(repository, eventPublisher)
    }

    void handle(Register_new_device command) {
        Device device = deviceWith(command.deviceId)

        device.apply(
                repository.getEventsFor(Device.class, command.deviceId))

        def thisEventPublisher = eventPublisher
        def unitOfWork = new Object() {
            def publish(Event event) {
                thisEventPublisher.publish(event)
                device.apply(event)
            }
        }
        device.handle(command, unitOfWork)
    }

    private Device deviceWith(UUID deviceId) {
        Device device = repository.getDevice(deviceId)
        return device ? device : new Device(deviceId)
    }
}
