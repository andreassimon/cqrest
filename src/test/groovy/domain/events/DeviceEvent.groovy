package domain.events

import domain.aggregates.Device

abstract class DeviceEvent extends Event<Device> {
    DeviceEvent() {
        super()
    }

    @Override
    String getAggregateClassName() {
        return Device.class.canonicalName
    }

}
