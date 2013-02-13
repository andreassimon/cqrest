package domain.events

import domain.aggregates.Device

class Device_was_registered extends DeviceEvent {
    final String deviceName

    Device_was_registered(Map attributes) {
        super()
        assert attributes != null
        deviceName = attributes.deviceName
    }

    @Override
    UUID getAggregateId() {
        return deviceId
    }

    // TODO How to implement applyTo()?
    //  * functional (like it is)
    //  * non-functional
    Device applyTo(Device device) {
        new Device()
    }

    // TODO Every Event must have a toString() and an equals() method
    @Override
    String toString() {
        "New device was registered: deviceName=$deviceName"
    }

    @Override
    boolean equals(Object that) {
        this.deviceName == that.deviceName
    }
}

