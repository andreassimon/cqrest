package commands

import domain.commands.*
import domain.events.*
import org.junit.*

class LockOutDeviceTest extends CommandSideTest {
    def deviceId = UUID.randomUUID()

    @Test
    void should_lock_out_existing_device() {
        given {
            New_device_was_registered(deviceId, "andreas-thinkpad")
        }

        when(new Lock_out_device(deviceId))

        then {
            Device_was_locked_out(deviceId)
        }
    }

    @Test(expected = UnknownDeviceException.class)
    void should_throw_Exception_when_locking_a_non_existent_device() {
        given {}

        when(new Lock_out_device(deviceId))
    }
}
