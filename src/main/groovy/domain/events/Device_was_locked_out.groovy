package domain.events

import domain.aggregates.Device

class Device_was_locked_out extends Event<Device> {
    final Device.Id deviceId

    Device_was_locked_out(Device.Id deviceId) {
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
    Device applyTo(device) {
        device
    }
}

