package domain.commands

class Lock_out_device {
    final UUID deviceId

    Lock_out_device(UUID deviceId) {
        this.deviceId = deviceId
    }
}


class UnknownDeviceException extends RuntimeException {}
