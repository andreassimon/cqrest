package domain.commands

import domain.aggregates.Device

class Lock_out_device {
    final Device.Id deviceId

    Lock_out_device(Device.Id deviceId) {
        this.deviceId = deviceId
    }
}


class UnknownDeviceException extends RuntimeException {}
