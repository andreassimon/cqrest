package infrastructure

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

class EventStoreTest {

    DataSource dataSource
    JdbcTemplate jdbcTemplate

    @Before
    public void setUp() throws Exception {
        dataSource = new PGSimpleDataSource()
        dataSource.user = 'user12'
        dataSource.password = '34klq*'
        dataSource.databaseName = 'one-os-cqrs'

        jdbcTemplate = new JdbcTemplate(dataSource)
        jdbcTemplate.execute("DROP TABLE IF EXISTS Events;")
        jdbcTemplate.execute("CREATE TABLE Events(AggregateClassName VARCHAR(255), AggregateId UUID, EventName VARCHAR(255), Attributes TEXT, Timestamp TIMESTAMP);")
    }

    @Test
    public void should_insert_new_event() throws Exception {
        EventStore eventStore = new EventStore()
        eventStore.jdbcTemplate = jdbcTemplate
        JdbcRepository repository = new JdbcRepository()
        repository.jdbcTemplate = jdbcTemplate
        final event = new New_device_was_registered(deviceId: randomUUID(), deviceName: "Device1")

        eventStore.save(event)

        final history = repository.getEventsFor(Device, event.deviceId)

        assertThat history, equalTo([
            new New_device_was_registered(deviceId: event.deviceId, deviceName: 'Device1')
        ])
    }



    @Test
    public void date_and_timestamp_should_be_comparable() throws Exception {
        long currentTimeMillis = System.currentTimeMillis()

        Date date = new Date(currentTimeMillis)
        Timestamp timestamp = new Timestamp(currentTimeMillis)

        assertThat date, equalTo(timestamp)
        assertThat timestamp, not(equalTo(date))
    }

}
