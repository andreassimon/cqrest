package oneos.test.domain.commands

class Unregister_device {
    final UUID deviceId

    Unregister_device(Map attributes) {
        this.deviceId = attributes.deviceId
    }
}
