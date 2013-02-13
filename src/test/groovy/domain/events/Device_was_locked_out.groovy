package domain.events

import domain.aggregates.Device

class Device_was_locked_out extends DeviceEvent {

    @Override
    String toString() {
        "Device was locked out"
    }

    @Override
    boolean equals(Object that) {
        this.toString() == that.toString()
    }

    @Override
    Device applyTo(Device device) {
        device
    }

    @Override
    UUID getAggregateId() {
        return deviceId
    }
}

