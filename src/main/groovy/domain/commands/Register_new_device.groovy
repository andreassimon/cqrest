package domain.commands

class Register_new_device {
    final UUID deviceId
    final String deviceName

    Register_new_device(UUID deviceId, String deviceName) {
        this.deviceId = deviceId
        this.deviceName = deviceName
    }
}


