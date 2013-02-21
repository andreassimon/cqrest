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
    static final String APPLICATION_NAME = 'CQRS Core Library'
    static final String BOUNDED_CONTEXT_NAME = 'Tests'
    static final String AGGREGATE_NAME = 'Device'

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

    @Test(expected = StaleStateException)
    void should_prevent_race_conditions() {
        eventStore.save(new EventEnvelope(
            APPLICATION_NAME,
            BOUNDED_CONTEXT_NAME,
            AGGREGATE_NAME,
            aggregateId,
            new BusinessAggregate_was_created()
        ))

        def instance1 = eventStore.getAggregate(
            APPLICATION_NAME,
            BOUNDED_CONTEXT_NAME,
            AGGREGATE_NAME,
            BusinessAggregate,
            aggregateId,
            'de.oneos.cqrs.eventstore.'
        )

        def instance2 = eventStore.getAggregate(
            APPLICATION_NAME,
            BOUNDED_CONTEXT_NAME,
            AGGREGATE_NAME,
            BusinessAggregate,
            aggregateId,
            'de.oneos.cqrs.eventstore.'
        )

        instance1.callBusinessMethod()

        instance2.callAnotherBusinessMethod()
    }

    static class BusinessAggregate {

        void callBusinessMethod() {
            publishEvent(new A_business_method_was_called())
        }

        void callAnotherBusinessMethod() {
            publishEvent(new Another_business_method_was_called())
        }
    }

    static class BusinessAggregate_was_created extends Event<BusinessAggregate> {
        BusinessAggregate applyTo(BusinessAggregate aggregate) { aggregate }
    }

    static class A_business_method_was_called extends Event<BusinessAggregate> {
        BusinessAggregate applyTo(BusinessAggregate aggregate) { aggregate }
    }

    static class Another_business_method_was_called extends Event<BusinessAggregate> {
        BusinessAggregate applyTo(BusinessAggregate aggregate) { aggregate }
    }
}
