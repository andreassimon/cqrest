package infrastructure

import domain.aggregates.Device
import domain.commands.Register_new_device
import domain.events.New_device_was_registered
import org.hamcrest.CoreMatchers
import org.junit.Assert
import org.junit.Test
import org.postgresql.ds.PGSimpleDataSource
import org.springframework.jdbc.core.JdbcTemplate

import javax.sql.DataSource
import java.sql.Timestamp

import static org.hamcrest.CoreMatchers.equalTo
import static org.hamcrest.CoreMatchers.not
import static org.junit.Assert.assertThat

class EventStoreTest {

    @Test
    public void should_insert_new_event() throws Exception {
        EventStore eventStore = new EventStore()
        DataSource dataSource = new PGSimpleDataSource()
        dataSource.user = 'user12'
        dataSource.password = '34klq*'
        dataSource.databaseName = 'one-os-cqrs'

        final jdbcTemplate = new JdbcTemplate(dataSource)
        jdbcTemplate.execute("DROP TABLE IF EXISTS events;")
        jdbcTemplate.execute("CREATE TABLE events(EventName VARCHAR(255), Attributes TEXT, Timestamp TIMESTAMP);")
        eventStore.jdbcTemplate = jdbcTemplate
        final event = new New_device_was_registered(UUID.randomUUID(), "Device1")
        eventStore.save(event)

        def repository = new Repository()
        repository.jdbcTemplate = jdbcTemplate
        final history = repository.getEventsFor(Device, event.deviceId)

        assertThat history, equalTo([
            [
                eventname: 'New device was registered',
                attributes: "{\"New device was registered\":{\"deviceId\":\"${event.deviceId}\",\"timestamp\":\"${event.timestamp}\",\"deviceName\":\"Device1\"}}",
                timestamp: new Timestamp(event.timestamp.time)
            ]
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
