package commands

import org.junit.*

import infrastructure.persistence.UnknownAggregate
import oneos.test.domain.commands.Lock_out_device

class LockOutDeviceTest extends CommandSideTest {
    def deviceId = UUID.randomUUID()

    @Test
    void should_lock_out_existing_device() {
        given {
            "oneos.test.domain.events.Device_was_registered"(
                    deviceId:  deviceId,
                    deviceName: "andreas-thinkpad")
        }

        when(new Lock_out_device(deviceId: deviceId))

        then {
            "oneos.test.domain.events.Device_was_locked_out"()
        }
    }

    @Test(expected = UnknownAggregate.class)
    void should_throw_Exception_when_locking_a_non_existent_device() {
        given {}

        when(new Lock_out_device(deviceId: deviceId))
    }
}
