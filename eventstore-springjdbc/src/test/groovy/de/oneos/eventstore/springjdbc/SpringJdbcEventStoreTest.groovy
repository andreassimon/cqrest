package de.oneos.eventstore.springjdbc

import org.junit.*
import static org.junit.Assert.*
import static org.mockito.Mockito.*
import static org.mockito.Matchers.*
import static org.hamcrest.Matchers.*

import java.sql.*
import javax.sql.*
import org.springframework.jdbc.core.*

import de.oneos.eventstore.*


class SpringJdbcEventStoreTest extends EventStore_ContractTest {

    EventStore eventStore
    Connection sentinelConnection
    JdbcOperations mockJdbcTemplate = mock(JdbcOperations)


    @Before
    void setUp() {
        super.setUp()

        DataSource dataSource = new org.h2.jdbcx.JdbcDataSource()
        dataSource.user = 'SA'
        dataSource.password = ''
        dataSource.url = 'jdbc:h2:mem:testDb;MVCC=TRUE;LOCK_TIMEOUT=10000'

        // See http://stackoverflow.com/questions/8907303/grails-throws-table-xxx-not-found
        // H2 closes the database when the last connection is closed. For an in-memory database,
        // closing the connection means the data is lost...
        // So if you keep one connection open all the time, then you should be fine. You could
        // call this a 'sentinel' connection.
        sentinelConnection = dataSource.getConnection()

        eventStore = new SpringJdbcEventStore(dataSource, SpringJdbcEventStore.CREATE_TABLE)
    }


    @After
    public void tearDown() {
        eventStore.dropTable()
        sentinelConnection.close()
    }


    @Test
    void should_have_a_defined_TABLE_NAME() {
        assertThat SpringJdbcEventStore.TABLE_NAME, equalTo('events')
    }

    @Test
    void createTable__should_create_the_events_table() {
        eventStore.jdbcTemplate = mockJdbcTemplate

        eventStore.createTable()

        verify(mockJdbcTemplate).execute(contains("CREATE TABLE IF NOT EXISTS ${SpringJdbcEventStore.TABLE_NAME}"))
    }

    @Test
    void dropTable__should_delete_the_events_table() {
        eventStore.jdbcTemplate = mockJdbcTemplate

        eventStore.dropTable()

        verify(mockJdbcTemplate).execute("DROP TABLE IF EXISTS ${SpringJdbcEventStore.TABLE_NAME};")
    }

}
