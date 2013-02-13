package oneos.test.domain.commands

class Register_device {
    final String deviceName

    Register_device(Map attributes) {
        this.deviceName = attributes.deviceName
    }
}
