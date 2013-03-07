package de.oneos.cqrs.eventstore.inmemory

import org.junit.*
import de.oneos.eventstore.EventStore_ContractTest


class InMemoryEventStoreTest extends EventStore_ContractTest {

    InMemoryEventStore eventStore

    @Before
    public void setUp() {
        eventStore = new InMemoryEventStore()
    }

}
