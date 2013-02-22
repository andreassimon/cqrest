package oneos.test.domain.commandhandler

import oneos.test.domain.commands.Lock_out_device
import oneos.test.domain.aggregates.Device
import domain.commandhandler.EventSourcingCommandHandler


class Lock_out_device_Handler extends EventSourcingCommandHandler<Lock_out_device> {

    protected getAggregate = { String applicationName, String boundedContextName, String aggregateName, Class aggregateClass, UUID aggregateId ->
        eventStore.getAggregate(applicationName, boundedContextName, aggregateName, aggregateClass, aggregateId, 'domain.events.')
    }

    protected getDevice = getAggregate.curry('CQRS Core Library', 'Test', 'Device', oneos.test.domain.aggregates.Device)

    void handle(Lock_out_device command) {
        Device device = getDevice(command.deviceId)

        collectEventsFrom(device) {
            lockOut()
        }
    }

}
