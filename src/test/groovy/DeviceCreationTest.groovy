import commands.*

import model.Device
import model.events.*

import org.junit.Test


class DeviceCreationTest extends CommandSideTest {

    @Test
    void should_create_new_device() {
        def newDeviceId = UUID.randomUUID()

        when(new Register_new_device(deviceId: newDeviceId, deviceName: "andreas-thinkpad"))

        then {
            New_device_was_registered(new Device.Id(newDeviceId), "andreas-thinkpad")
        }
    }

}

