package commands

import org.junit.Test
import domain.commands.Unregister_new_device

class UnregisterNewDeviceTest extends CommandSideTest {
    def deviceUUID = UUID.randomUUID()

    @Test
    void should_create_new_device() {
        given {
            New_device_was_registered(deviceId: deviceUUID, deviceName: "andreas-thinkpad")
        }

        when(new Unregister_new_device(deviceId: deviceUUID))

        then {
            Device_was_unregistered(deviceId: deviceUUID)
        }
    }

}

