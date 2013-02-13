package infrastructure.persistence;

import org.junit.Test
import domain.events.EventEnvelope
import org.h2.jdbcx.JdbcDataSource
import org.springframework.jdbc.core.JdbcTemplate
import javax.sql.DataSource
import java.sql.Connection

import static org.junit.Assert.assertThat
import static org.hamcrest.Matchers.notNullValue
import oneos.test.domain.events.Device_was_registered
import oneos.test.domain.aggregates.Device;

public class PersistentEventPublisherTest {
    private static final String APPLICATION_NAME = 'CQRS Core Library'
    private static final String BOUNDED_CONTEXT_NAME = 'Tests'
    private static final String AGGREGATE_NAME = 'Device'

    @Test
    public void should_save_the_event_to_the_event_store() {
        DataSource dataSource = new JdbcDataSource()
        dataSource.user = 'SA'
        dataSource.password = ''
        dataSource.url = 'jdbc:h2:mem:testDb;MVCC=TRUE;LOCK_TIMEOUT=10000'

        // See http://stackoverflow.com/questions/8907303/grails-throws-table-xxx-not-found
        // H2 closes the database when the last connection is closed. For an in-memory database,
        // closing the connection means the data is lost...
        // So if you keep one connection open all the time, then you should be fine. You could
        // call this a 'sentinel' connection.
        Connection sentinelConnection = dataSource.getConnection()

        JdbcTemplate jdbcTemplate = new JdbcTemplate(dataSource)

        EventStore eventStore = new JdbcEventStore(jdbcTemplate: jdbcTemplate)
        eventStore.createTable()

        PersistentEventPublisher eventPublisher = new PersistentEventPublisher(eventStore)

        def deviceId = UUID.randomUUID()
        eventPublisher.publish(new EventEnvelope(APPLICATION_NAME, BOUNDED_CONTEXT_NAME, AGGREGATE_NAME, deviceId, new Device_was_registered(deviceId: deviceId, deviceName: 'new device')))

        assertThat eventStore.getAggregate(APPLICATION_NAME, BOUNDED_CONTEXT_NAME, AGGREGATE_NAME, Device, deviceId, 'oneos.test.domain.events.'), notNullValue()
    }
}
