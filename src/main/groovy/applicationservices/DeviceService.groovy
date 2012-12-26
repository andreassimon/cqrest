package applicationservices

import domainservices.EventHandler
import model.Device
import model.events.Device_asked_for_updates
import model.events.Device_was_locked_out
import model.events.New_device_was_registered


class DeviceService {

    EventHandler eventHandler

    def registerNewDevice(deviceId, deviceName, devicePublicKey) {
        eventHandler.dispatch(new New_device_was_registered(deviceId, deviceName, devicePublicKey))
    }

    def lockOutDevice(deviceId) {
        eventHandler.dispatch(new Device_was_locked_out(deviceId))
    }

    def getDevices(offset, count) {
        readModel.getDevices(offset, count)
    }
}
