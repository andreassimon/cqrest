package events

import domain.aggregates.Device
import domain.events.New_device_was_registered
import org.hamcrest.CoreMatchers
import org.junit.Assert
import org.junit.Test

import static java.util.UUID.randomUUID

class New_device_was_registered_Test {

    def deviceId = randomUUID()
    def deviceName = "deviceName"


    @Test
    public void events_are_equal_when_device_id_and_deviceName_are_equal() {
        def event1 = new New_device_was_registered(deviceId: deviceId, deviceName: deviceName)
        def event2 = new New_device_was_registered(deviceId: deviceId, deviceName: deviceName)

        Assert.assertThat(event1, CoreMatchers.equalTo(event2))
    }

}
