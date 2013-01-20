package commands

import org.junit.*
import domain.commands.Register_new_device

class RegisterNewDeviceTest extends CommandSideTest {

    @Test
    void should_create_new_device() {
        def newDeviceUUID = UUID.randomUUID()

        when(new Register_new_device(deviceId: newDeviceUUID, deviceName: "andreas-thinkpad"))

        then {
            New_device_was_registered(deviceId: newDeviceUUID, deviceName: "andreas-thinkpad")
        }
    }

}

