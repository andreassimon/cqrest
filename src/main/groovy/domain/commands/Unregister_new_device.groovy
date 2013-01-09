package domain.commands

class Unregister_new_device {
    final UUID deviceId

    Unregister_new_device(Map attributes) {
        this.deviceId = attributes.deviceId
    }
}


