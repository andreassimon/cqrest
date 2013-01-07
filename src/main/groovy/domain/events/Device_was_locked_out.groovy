package domain.events

import domain.aggregates.Device

class Device_was_locked_out extends Event<Device> {
    final UUID deviceId

    Device_was_locked_out(UUID deviceId) {
        this.deviceId = deviceId
    }

    @Override
    String toString() {
        "Device was locked out: deviceId=$deviceId"
    }

    @Override
    boolean equals(Object that) {
        this.toString() == that.toString()
    }

    @Override
    Device applyTo(Device device) {
        device
    }
}

