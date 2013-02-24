package de.oneos.eventstore.springjdbc

import de.oneos.eventstore.*
import org.junit.*

class SpringJdbcEventStoreTest extends EventStore_ContractTest {

    EventStore eventStore

    @Before
    void setUp() {
        eventStore = new SpringJdbcEventStore()
    }

}
