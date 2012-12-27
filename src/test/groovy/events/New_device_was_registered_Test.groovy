package events

import domain.aggregates.Device
import domain.events.New_device_was_registered
import org.hamcrest.CoreMatchers
import org.junit.Assert
import org.junit.Test

class New_device_was_registered_Test {

    def deviceId = new Device.Id()
    def deviceName = "deviceName"


    @Test
    public void events_are_equal_when_device_id_and_deviceName_are_equal() {
        def event1 = new New_device_was_registered(deviceId, deviceName)
        def event2 = new New_device_was_registered(deviceId, deviceName)

        Assert.assertThat(event1, CoreMatchers.equalTo(event2))
    }

}
