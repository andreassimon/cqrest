package domain.commands

class Register_device {
    final UUID deviceId
    final String deviceName

    Register_device(Map attributes) {
        this.deviceId = attributes.deviceId
        this.deviceName = attributes.deviceName
    }
}
