package infrastructure

import domain.commands.Register_new_device
import domain.events.New_device_was_registered
import org.junit.Test
import org.postgresql.ds.PGSimpleDataSource
import org.springframework.jdbc.core.JdbcTemplate

import javax.sql.DataSource

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
        eventStore.save(new New_device_was_registered(UUID.randomUUID(), "Device1"))


    }
}
