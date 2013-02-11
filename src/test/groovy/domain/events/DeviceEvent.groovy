package domain.events

import domain.aggregates.Device

abstract class DeviceEvent extends Event<Device> {
    final String applicationName    = "CQRS Core Library"
    final String boundedContextName = "Tests"
    final String aggregateName      = Device.simpleName

    final Class<Device> aggregateClass = Device

    DeviceEvent() {
        super()
    }

}
