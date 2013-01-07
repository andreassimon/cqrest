package commands

import domain.commands.Register_new_device
import org.junit.*

class RegisterNewDeviceTest extends CommandSideTest {

    @Test
    void should_create_new_device() {
        def newDeviceUUID = UUID.randomUUID()

        when(new Register_new_device(newDeviceUUID, "andreas-thinkpad"))

        then {
            New_device_was_registered(newDeviceUUID, "andreas-thinkpad")
        }
    }

}

