package de.oneos.eventstore.inmemory

import org.junit.*
import de.oneos.eventstore.*


class InMemoryEventStoreTest extends EventStore_ContractTest {

    InMemoryEventStore eventStore

    @Before
    public void setUp() {
        super.setUp()

        eventStore = new InMemoryEventStore(APPLICATION_NAME, BOUNDED_CONTEXT_NAME)
    }

}
