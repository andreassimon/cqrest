package infrastructure.utilities

import org.junit.Test

import static java.util.UUID.randomUUID
import static org.hamcrest.CoreMatchers.equalTo
import static org.junit.Assert.assertThat
import oneos.test.domain.events.Device_was_registered

class GenericEventSerializerTest {

    @Test
    public void should_serialize_to_JSON() {
        def event = new Device_was_registered(deviceId: randomUUID(), deviceName: "device name")

        assertThat(GenericEventSerializer.toJSON(event), equalTo("""{"deviceName":"${event.deviceName}"}""".toString()))
    }
}
