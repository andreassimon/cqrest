package de.oneos.cqrs.eventstore.inmemory

import oneos.test.domain.aggregates.Device
import org.junit.Before
import org.junit.Test

import static java.util.UUID.randomUUID
import static org.hamcrest.Matchers.notNullValue
import static org.junit.Assert.assertThat

class InMemoryEventStoreTest {

    InMemoryEventStore eventStore
    String APPLICATION_NAME = 'APPLICATION_NAME'
    String BOUNDED_CONTEXT_NAME = 'BOUNDED_CONTEXT_NAME'
    String AGGREGATE_NAME = 'AGGREGATE_NAME'

    @Before
    public void setUp() {
        eventStore = new InMemoryEventStore()
    }

    @Test
    void should_add_unitOfWork_property_to_aggregates() {
        UUID deviceUUID = randomUUID()
        eventStore.history = [
            new oneos.test.domain.events.Device_was_registered(deviceId: deviceUUID, deviceName: "andreas-thinkpad")
        ]

        def aggregate = eventStore.getAggregate(
            APPLICATION_NAME,
            BOUNDED_CONTEXT_NAME,
            AGGREGATE_NAME,
            Device,
            deviceUUID,
            'oneos.test.domain.events.'
        )

        assertThat aggregate.unitOfWork, notNullValue()
    }

}
