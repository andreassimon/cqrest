package de.oneos.cqrs.eventstore.springjdbc

import domain.events.EventEnvelope

import org.h2.jdbcx.JdbcDataSource
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.springframework.jdbc.core.JdbcTemplate

import java.sql.Connection
import java.sql.Timestamp
import javax.sql.DataSource

import static java.util.UUID.randomUUID
import static org.hamcrest.CoreMatchers.*
import static org.junit.Assert.assertThat
import oneos.test.domain.events.Device_was_registered
import oneos.test.domain.aggregates.Device

class SpringJdbcEventStoreTest {

    DataSource dataSource
    JdbcTemplate jdbcTemplate
    SpringJdbcEventStore eventStore
    Connection sentinelConnection

    @Before
    public void setUp() throws Exception {
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

    @After
    public void tearDown() {
        sentinelConnection.close()
    }

    @Test
    public void should_insert_new_event() throws Exception {
        def deviceId = randomUUID()
        final event = new Device_was_registered(deviceName: "Device1")
        final eventEnvelope = new EventEnvelope<Device>('CQRS Core Library', 'Tests', 'Device', deviceId, event)
        eventStore.save(eventEnvelope)

        final history = eventStore.getEventsFor('CQRS Core Library', 'Tests', 'Device', deviceId, 'oneos.test.domain.events.')

        assertThat history, equalTo([
            new Device_was_registered(deviceName: 'Device1')
        ])
    }



    @Test
    public void date_and_timestamp_are_not_mutually_comparable() throws Exception {
        long currentTimeMillis = System.currentTimeMillis()

        Date date = new Date(currentTimeMillis)
        Timestamp timestamp = new Timestamp(currentTimeMillis)

        assertThat date, equalTo(timestamp)
        assertThat timestamp, not(equalTo(date))
    }

}
