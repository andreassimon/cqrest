package domain.events

import domain.aggregates.Device

abstract class DeviceEvent extends domain.events.Event<Device> {
    DeviceEvent() {
        super()
    }

    @Override
    String getAggregateClassName() {
        return Device.class.canonicalName
    }

}
