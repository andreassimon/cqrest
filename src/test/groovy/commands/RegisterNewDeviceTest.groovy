package commands

import domain.aggregates.Device
import domain.commands.Register_new_device
import org.junit.Test

class RegisterNewDeviceTest extends CommandSideTest {

    @Test
    void should_create_new_device() {
        def newDeviceUUID = UUID.randomUUID()
        def newDeviceId = new Device.Id(newDeviceUUID)

        when(new Register_new_device(newDeviceUUID, "andreas-thinkpad"))

        then {
            New_device_was_registered(newDeviceId, "andreas-thinkpad")
        }
    }

}

