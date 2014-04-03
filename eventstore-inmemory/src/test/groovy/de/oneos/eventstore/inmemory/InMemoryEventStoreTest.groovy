package de.oneos.eventstore.inmemory

import org.junit.Before

import de.oneos.eventstore.EventStore_ContractTest


class InMemoryEventStoreTest extends EventStore_ContractTest {

    InMemoryEventStore eventStore

    @Override
    @Before
    public void setUp() {
        super.setUp()

        eventStore = new InMemoryEventStore()
    }

}
