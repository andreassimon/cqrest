import commands.Register_new_device
import model.Device
import model.events.Event
import model.events.New_device_was_registered
import org.hamcrest.CoreMatchers
import org.junit.Assert
import org.junit.Before
import org.junit.Test

import static org.hamcrest.CoreMatchers.any
import static org.hamcrest.CoreMatchers.equalTo
import static org.junit.Assert.assertThat

class DeviceCreationTest {

    def repository = new Repository()
    def eventPublisher = new EventPublisher()

    def commandRouter


    @Before
    public void setUp() throws Exception {
        commandRouter = new CommandRouter()

        commandRouter.repository = repository
        commandRouter.eventPublisher = eventPublisher
    }


    @Test
    void should_create_new_device() {
        def newDeviceId = UUID.randomUUID()
        commandRouter.route(new Register_new_device(deviceId: newDeviceId, deviceName: "andreas-thinkpad"))

        assertThat(eventPublisher.receivedEvents, equalTo([new New_device_was_registered(new Device.Id(newDeviceId), "andreas-thinkpad")]))
    }

    @Test
    public void event_lists_should_be_equal() {
        def newDeviceUUID = UUID.randomUUID()
        def newDeviceId = new Device.Id(newDeviceUUID)

        assertThat([new New_device_was_registered(newDeviceId, "andreas-thinkpad")], equalTo([new New_device_was_registered(newDeviceId, "andreas-thinkpad")]))
    }
}

class Repository {
    Device getDevice(deviceId) {
        return null
    }

    def getEventsFor(Class clazz, UUID entityId) {
        return []
    }
}

class EventPublisher {
    def receivedEvents = []

    void publish(Event<?> event) {
        receivedEvents << event
    }

}
