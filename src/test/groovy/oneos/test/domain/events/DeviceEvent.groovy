package oneos.test.domain.events

import oneos.test.domain.aggregates.Device
import domain.events.Event


abstract class DeviceEvent extends Event<Device> {
    final String applicationName    = "CQRS Core Library"
    final String boundedContextName = "Tests"
    final String aggregateName      = Device.simpleName

    final Class<Device> aggregateClass = Device

    DeviceEvent() {
        super()
    }

}
