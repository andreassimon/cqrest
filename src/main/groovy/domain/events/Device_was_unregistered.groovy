package domain.events

import domain.aggregates.Device

class Device_was_unregistered extends Event<Device> {
    final Device.Id deviceId

    Device_was_unregistered(Device.Id deviceId) {
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
