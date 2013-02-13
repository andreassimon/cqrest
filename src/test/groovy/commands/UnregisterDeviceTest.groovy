package commands

import org.junit.Test
import domain.commands.Unregister_device

class UnregisterDeviceTest extends CommandSideTest {
    def deviceUUID = UUID.randomUUID()

    @Test
    void should_unregister_the_device() {
        given {
            Device_was_registered(deviceId: deviceUUID, deviceName: "andreas-thinkpad")
        }

        when(new Unregister_device(deviceId: deviceUUID))

        then {
            Device_was_unregistered(deviceId: deviceUUID)
        }
    }

}

