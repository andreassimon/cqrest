package model.events

import model.Device

abstract class Event<T> {
    abstract void applyTo(T)
}

class New_device_was_registered extends Event<Device> {
    private final Device.Id deviceId
    private final String deviceName

    New_device_was_registered(Device.Id deviceId, String deviceName) {
        this.deviceName = deviceName
        this.deviceId = deviceId
    }

    void applyTo(device) {

    }

    @Override
    String toString() {
        "New device was registered: deviceId=$deviceId, deviceName=$deviceName"
    }
}

