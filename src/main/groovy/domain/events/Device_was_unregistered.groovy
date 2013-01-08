package domain.events

import domain.aggregates.Device

class Device_was_unregistered extends DeviceEvent {
    final UUID deviceId

    Device_was_unregistered(UUID deviceId) {
        this.deviceId = deviceId
    }

    @Override
    Device applyTo(Device device) {
        device.unregistered = true
    }

    @Override
    UUID getAggregateId() {
        return deviceId
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
