package commands

import oneos.test.domain.commands.Register_device
import org.junit.Test

class RegisterDeviceTest extends CommandSideTest {

    @Test
    void should_create_new_device() {
        when(new Register_device(deviceName: "andreas-thinkpad"))

        then {
            "oneos.test.domain.events.Device_was_registered"(deviceName: "andreas-thinkpad")
        }
    }

}

