package domain.commands

class Register_new_device {
    final UUID deviceId
    final String deviceName

    Register_new_device(Map attributes) {
        this.deviceId = attributes.deviceId
        this.deviceName = attributes.deviceName
    }
}


