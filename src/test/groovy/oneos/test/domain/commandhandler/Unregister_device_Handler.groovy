package oneos.test.domain.commandhandler

import oneos.test.domain.commands.Unregister_device
import domain.commandhandler.EventSourcingCommandHandler


class Unregister_device_Handler extends EventSourcingCommandHandler<Unregister_device> {

    protected getAggregate = { String applicationName, String boundedContextName, String aggregateName, Class aggregateClass, UUID aggregateId ->
        eventStore.getAggregate(applicationName, boundedContextName, aggregateName, aggregateClass, aggregateId, 'domain.events.')
    }

    protected getDevice = getAggregate.curry('CQRS Core Library', 'Test', 'Device', oneos.test.domain.aggregates.Device)

    void handle(Unregister_device command) {
        collectEventsFrom(getDevice(command.deviceId)) {
            unregister(command.deviceId)
        }
    }

}
