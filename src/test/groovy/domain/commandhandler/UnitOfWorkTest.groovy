package domain.commandhandler;

import org.junit.Test
import framework.EventPublisher

import static org.mockito.Mockito.mock
import domain.aggregates.Device
import domain.events.Device_was_registered

import static org.mockito.Mockito.verify
import domain.events.EventEnvelope

import static java.util.UUID.randomUUID
import static org.mockito.Mockito.any;

public class UnitOfWorkTest {
    @Test
    public void publishEvent__should_copy_application_name_from_aggregate() throws Exception {
        EventPublisher eventPublisher = mock(EventPublisher)
        UnitOfWork unitOfWork = new UnitOfWork(eventPublisher)

        def deviceId = randomUUID()
        def event = new Device_was_registered(deviceId: deviceId, deviceName: 'New device')
        unitOfWork.append(Device, deviceId, event)

        unitOfWork.flush()

        verify(eventPublisher).publish(new EventEnvelope(Device.applicationName, Device.boundedContextName, Device.aggregateName, deviceId, event, any(Date)))
    }
}
