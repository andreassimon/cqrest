package infrastructure.utilities

import domain.aggregates.Device
import domain.events.New_device_was_registered
import org.hamcrest.CoreMatchers
import org.junit.Assert
import org.junit.Test

import static org.hamcrest.CoreMatchers.equalTo
import static org.junit.Assert.assertThat

class GenericEventSerializerTest {

    @Test
    public void should_serialize_to_JSON() {
        def event = new New_device_was_registered(new Device.Id(), "device name")

        assertThat(GenericEventSerializer.toJSON(event), equalTo("{\"New device was registered\":{\"deviceId\":\"${event.deviceId}\",\"deviceName\":\"${event.deviceName}\"}}".toString()))
    }
}
