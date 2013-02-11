package infrastructure.persistence

import org.junit.*
import org.postgresql.ds.PGSimpleDataSource
import org.springframework.jdbc.core.JdbcTemplate

import javax.sql.DataSource
import java.sql.Timestamp

import static java.util.UUID.randomUUID
import static org.hamcrest.CoreMatchers.*
import static org.junit.Assert.assertThat
import domain.events.New_device_was_registered
import domain.aggregates.Device
import domain.events.EventEnvelope
import infrastructure.persistence.JdbcEventStore

class EventStoreTest {

    DataSource dataSource
    JdbcTemplate jdbcTemplate
    JdbcEventStore eventStore

    @Before
    public void setUp() throws Exception {
        dataSource = new PGSimpleDataSource()
        dataSource.user = 'user12'
        dataSource.password = '34klq*'
        dataSource.databaseName = 'one-os-cqrs'

        jdbcTemplate = new JdbcTemplate(dataSource)

        eventStore = new JdbcEventStore(jdbcTemplate: jdbcTemplate)

        eventStore.createTable()
    }

    @Test
    public void should_insert_new_event() throws Exception {
        def deviceId = randomUUID()
        final event = new New_device_was_registered(deviceId: deviceId, deviceName: "Device1")
        final eventEnvelope = new EventEnvelope<Device>('CQRS Core Library', 'Tests', 'Device', deviceId, event)
        eventStore.save(eventEnvelope)

        final history = eventStore.getEventsFor('CQRS Core Library', 'Tests', 'Device', event.deviceId)

        assertThat history, equalTo([
            new New_device_was_registered(deviceId: event.deviceId, deviceName: 'Device1')
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
