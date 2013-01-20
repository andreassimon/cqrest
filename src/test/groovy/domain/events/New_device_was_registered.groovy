package domain.events

import domain.aggregates.Device

class New_device_was_registered extends DeviceEvent {
    final UUID deviceId
    final String deviceName

    New_device_was_registered(Map attributes) {
        super()
        assert attributes != null
        deviceId = toUUID(attributes.deviceId)
        deviceName = attributes.deviceName
    }

    def toUUID(String uuid) {
        UUID.fromString(uuid)
    }

    def toUUID(UUID uuid) {
        uuid
    }

    @Override
    UUID getAggregateId() {
        return deviceId
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

