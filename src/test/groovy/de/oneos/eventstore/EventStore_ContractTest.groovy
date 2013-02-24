package de.oneos.eventstore

import org.junit.*
import static org.junit.Assert.*
import static org.hamcrest.Matchers.*


abstract class EventStore_ContractTest {

    abstract EventStore getEventStore()

    @Test
    void should_provide_UnitOfWork_instances() {
        assertThat eventStore.createUnitOfWork(), notNullValue()
    }

}
