package de.oneos.cqrs.eventstore.springjdbc

import de.oneos.cqrs.eventstore.EventStore_ContractTest
import org.h2.jdbcx.JdbcDataSource
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.springframework.jdbc.core.JdbcTemplate

import java.sql.Connection
import java.sql.Timestamp
import javax.sql.DataSource

import static org.hamcrest.Matchers.equalTo
import static org.hamcrest.Matchers.not
import static org.junit.Assert.assertThat

class SpringJdbcEventStoreTest extends EventStore_ContractTest {

    DataSource dataSource
    JdbcTemplate jdbcTemplate
    Connection sentinelConnection

    @Before
    public void setUp() {
        dataSource = new JdbcDataSource()
        dataSource.user = 'SA'
        dataSource.password = ''
        dataSource.url = 'jdbc:h2:mem:testDb;MVCC=TRUE;LOCK_TIMEOUT=10000'

        // See http://stackoverflow.com/questions/8907303/grails-throws-table-xxx-not-found
        // H2 closes the database when the last connection is closed. For an in-memory database,
        // closing the connection means the data is lost...
        // So if you keep one connection open all the time, then you should be fine. You could
        // call this a 'sentinel' connection.
        sentinelConnection = dataSource.getConnection()

        jdbcTemplate = new JdbcTemplate(dataSource)

        eventStore = new SpringJdbcEventStore(jdbcTemplate: jdbcTemplate)

        eventStore.createTable()
    }

    @Test
    public void date_and_timestamp_are_not_mutually_comparable() throws Exception {
        long currentTimeMillis = System.currentTimeMillis()

        Date date = new Date(currentTimeMillis)
        Timestamp timestamp = new Timestamp(currentTimeMillis)

        assertThat date, equalTo(timestamp)
        assertThat timestamp, not(equalTo(date))
    }

    @After
    public void tearDown() {
        sentinelConnection.close()
    }

}
