package domain.events

import domain.aggregates.Device

class Device_was_unregistered extends Event<Device> {
    final UUID deviceId

    Device_was_unregistered(UUID deviceId) {
        this.deviceId = deviceId
    }

    @Override
    Device applyTo(Device device) {
        device.unregistered = true
    }

    @Override
    String toString() {
        "$name: deviceId=$deviceId"
    }

    @Override
    boolean equals(Object that) {
        this.deviceId == that.deviceId
    }
}
