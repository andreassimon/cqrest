package de.oneos.cqrs.eventstore

import domain.events.EventEnvelope
import oneos.test.domain.aggregates.Device
import oneos.test.domain.events.Device_was_registered
import org.junit.Test

import static java.util.UUID.randomUUID
import static org.hamcrest.CoreMatchers.equalTo
import static org.junit.Assert.assertThat

abstract class EventStore_ContractTest {
    EventStore eventStore

    @Test
    public void should_insert_new_event() throws Exception {
        def deviceId = randomUUID()
        final event = new Device_was_registered(deviceName: "Device1")
        final eventEnvelope = new EventEnvelope<Device>('CQRS Core Library', 'Tests', 'Device', deviceId, event)
        eventStore.save(eventEnvelope)

        final history = eventStore.getEventsFor('CQRS Core Library', 'Tests', 'Device', deviceId, 'oneos.test.domain.events.')

        assertThat history, equalTo([
            new Device_was_registered(deviceName: 'Device1')
        ])
    }

}
