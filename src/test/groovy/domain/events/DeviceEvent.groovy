package domain.events

import domain.aggregates.Device

abstract class DeviceEvent extends Event<Device> {
    final Class<Device> aggregateClass = Device

    DeviceEvent() {
        super()
    }

}
