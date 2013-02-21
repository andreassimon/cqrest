package oneos.test.domain.events

import oneos.test.domain.aggregates.Device

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

    Device applyTo(Device device) {
        device
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

