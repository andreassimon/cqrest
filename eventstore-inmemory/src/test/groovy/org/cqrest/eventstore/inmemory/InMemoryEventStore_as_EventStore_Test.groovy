package org.cqrest.eventstore.inmemory

import org.junit.Before

import org.cqrest.eventstore.EventStore_ContractTest


class InMemoryEventStore_as_EventStore_Test extends EventStore_ContractTest {

    InMemoryEventStore eventStore

    @Override
    @Before
    public void setUp() {
        super.setUp()

        eventStore = new InMemoryEventStore()
    }

}
