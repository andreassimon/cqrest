package de.oneos.eventstore.springjdbc

import org.junit.*
import static org.junit.Assert.*
import static org.mockito.Mockito.*
import static org.mockito.Matchers.*
import static org.hamcrest.Matchers.*

import org.springframework.jdbc.core.*

import de.oneos.eventstore.*


class SpringJdbcEventStoreTest extends EventStore_ContractTest {

    EventStore eventStore
    JdbcOperations mockJdbcTemplate = mock(JdbcOperations)


    @Before
    void setUp() {
        eventStore = new SpringJdbcEventStore()
    }

    @Test
    void should_have_a_defined_TABLE_NAME() {
        assertThat SpringJdbcEventStore.TABLE_NAME, equalTo('events')
    }

    @Test
    void createTable__should_create_the_events_table() {
        eventStore.jdbcTemplate = mockJdbcTemplate

        eventStore.createTable()

        verify(mockJdbcTemplate).execute(contains("CREATE TABLE ${SpringJdbcEventStore.TABLE_NAME}"))
    }

    @Test
    void dropTable__should_delete_the_events_table() {
        eventStore.jdbcTemplate = mockJdbcTemplate

        eventStore.dropTable()

        verify(mockJdbcTemplate).execute("DROP TABLE IF EXISTS ${SpringJdbcEventStore.TABLE_NAME};")
    }

}
