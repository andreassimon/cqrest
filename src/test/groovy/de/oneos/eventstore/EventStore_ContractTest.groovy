package de.oneos.eventstore

import de.oneos.eventstore.springjdbc.SpringJdbcEventStore
import org.junit.Before
import org.junit.Test

import static org.hamcrest.Matchers.notNullValue
import static org.junit.Assert.assertThat

class EventStore_ContractTest {

    EventStore _eventStore

    EventStore getEventStore() {
        return _eventStore
    }

    @Before
    void setUp() {
        _eventStore = new SpringJdbcEventStore()
    }

    @Test
    void should_provide_UnitOfWork_instances() {
        EventStore eventStore = this.eventStore

        UnitOfWork unitOfWork = eventStore.createUnitOfWork()

        assertThat unitOfWork, notNullValue()
    }

}
