package domain.events

import domain.aggregates.Device

class New_device_was_registered extends Event<Device> {
    final UUID deviceId
    final String deviceName

    New_device_was_registered(UUID deviceId, String deviceName) {
        this.deviceId = deviceId
        this.deviceName = deviceName
    }

    // TODO How to implement applyTo()?
    //  * functional (like it is)
    //  * non-functional
    Device applyTo(Device device) {
        new Device(deviceId)
    }

    // TODO Every Event must have a toString() and an equals() method
    @Override
    String toString() {
        "New device was registered: deviceId=$deviceId, deviceName=$deviceName"
    }

    @Override
    boolean equals(Object that) {
        this.deviceId == that.deviceId &&
            this.deviceName == that.deviceName
    }
}

