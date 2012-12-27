package applicationservices

import domain.commands.CommandRouter
import domain.commands.Lock_out_device
import domain.commands.Register_new_device


class DeviceService {

    CommandRouter commandRouter

    def registerNewDevice(deviceId, deviceName, devicePublicKey) {
        commandRouter.route(new Register_new_device(deviceId, deviceName, devicePublicKey))
    }

    def lockOutDevice(deviceId) {
        commandRouter.route(new Lock_out_device(deviceId))
    }

    def getDevices(offset, count) {
        readModel.getDevices(offset, count)
    }

}
