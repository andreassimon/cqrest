package de.oneos.cqrs.eventstore

import domain.events.EventEnvelope
import oneos.test.domain.aggregates.Device
import oneos.test.domain.events.Device_was_registered
import org.junit.Test

import static java.util.UUID.randomUUID
import static org.hamcrest.CoreMatchers.equalTo
import static org.junit.Assert.assertThat
import domain.events.Event

abstract class EventStore_ContractTest {
    protected static final String APPLICATION_NAME = 'CQRS Core Library'
    protected static final String BOUNDED_CONTEXT_NAME = 'Tests'
    protected static final String AGGREGATE_NAME = 'Device'
    EventStore eventStore

    UUID aggregateId = randomUUID()
    EventEnvelope eventEnvelope = new EventEnvelope<Device>(APPLICATION_NAME, BOUNDED_CONTEXT_NAME, AGGREGATE_NAME, aggregateId, aBusinessEvent())


    @Test
    void should_insert_new_event() throws Exception {
        eventStore.save(eventEnvelope)

        List<Event> history = eventStore.getEventsFor(
            eventEnvelope.applicationName,
            eventEnvelope.boundedContextName,
            eventEnvelope.aggregateName,
            eventEnvelope.aggregateId,
            'oneos.test.domain.events.'
        )

        assertThat history, equalTo([
            eventEnvelope.event
        ])
    }

    private aBusinessEvent() {
        new Device_was_registered(deviceName: "Device1")
    }

    @Test
    void should_prevent_race_conditions() {

    }



}
