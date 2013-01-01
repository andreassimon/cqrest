package commands

import domain.aggregates.Device
import domain.commands.Register_new_device
import domain.commands.Unregister_new_device
import org.junit.Test

class UnregisterNewDeviceTest extends CommandSideTest {
    def deviceUUID = UUID.randomUUID()
    def deviceId = new Device.Id(deviceUUID)

    @Test
    void should_create_new_device() {
        given {
            New_device_was_registered(deviceId, "andreas-thinkpad")
        }

        when(new Unregister_new_device(deviceUUID))

        then {
            Device_was_unregistered(deviceId)
        }
    }

}

