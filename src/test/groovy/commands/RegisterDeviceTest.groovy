package commands

import org.junit.*

import domain.commands.Register_device

class RegisterDeviceTest extends CommandSideTest {

    @Test
    void should_create_new_device() {
        def newDeviceUUID = UUID.randomUUID()

        when(new Register_device(deviceId: newDeviceUUID, deviceName: "andreas-thinkpad"))

        then {
            Device_was_registered(deviceId: newDeviceUUID, deviceName: "andreas-thinkpad")
        }
    }

}

