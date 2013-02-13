package commands

import org.junit.*

import domain.commands.Register_device
import org.mockito.Matchers

import static org.hamcrest.Matchers.equalTo
import static org.junit.Assert.assertThat

class RegisterDeviceTest extends CommandSideTest {

    @Test
    void should_create_new_device() {
        when(new Register_device(deviceName: "andreas-thinkpad"))

        then {
            Device_was_registered(deviceName: "andreas-thinkpad")
        }
    }

}

