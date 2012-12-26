package model.events

import model.Device

class New_device_was_registered extends Event<Device> {
    private final Device.Id deviceId
    private final String deviceName

    New_device_was_registered(Device.Id deviceId, String deviceName) {
        this.deviceName = deviceName
        this.deviceId = deviceId
    }

    Device applyTo(device) {
        new Device(deviceId)
    }

    // TODO Every Event has to have a toString() and an equals() method
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

