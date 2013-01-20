package domain.commands

class Lock_out_device {
    final UUID deviceId

    Lock_out_device(Map attributes) {
        this.deviceId = attributes.deviceId
    }
}


class UnknownDeviceException extends RuntimeException {}
