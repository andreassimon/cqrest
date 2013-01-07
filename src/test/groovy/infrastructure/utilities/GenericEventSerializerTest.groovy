package infrastructure.utilities

import domain.events.New_device_was_registered
import org.junit.*

import static java.util.UUID.randomUUID
import static org.hamcrest.CoreMatchers.equalTo
import static org.junit.Assert.assertThat

class GenericEventSerializerTest {

    @Test
    public void should_serialize_to_JSON() {
        def event = new New_device_was_registered(randomUUID(), "device name")

        assertThat(GenericEventSerializer.toJSON(event), equalTo("{\"New device was registered\":{\"deviceId\":\"${event.deviceId}\",\"timestamp\":\"${event.timestamp}\",\"deviceName\":\"${event.deviceName}\"}}".toString()))
    }
}
