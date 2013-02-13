package commands

import org.junit.*
import domain.commands.Lock_out_device

import infrastructure.persistence.UnknownAggregate

class LockOutDeviceTest extends CommandSideTest {
    def deviceId = UUID.randomUUID()

    @Test
    void should_lock_out_existing_device() {
        given {
            Device_was_registered(
                    deviceId:  deviceId,
                    deviceName: "andreas-thinkpad")
        }

        when(new Lock_out_device(deviceId: deviceId))

        then {
            Device_was_locked_out(deviceId)
        }
    }

    @Test(expected = UnknownAggregate.class)
    void should_throw_Exception_when_locking_a_non_existent_device() {
        given {}

        when(new Lock_out_device(deviceId: deviceId))
    }
}
