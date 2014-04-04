package de.oneos.eventstore.springjdbc

import java.sql.Connection
import javax.sql.DataSource

import org.junit.Before
import org.junit.Ignore

import de.oneos.eventsourcing.EventEnvelope
import de.oneos.eventsourcing.EventStream

import org.cqrest.eventstore.EventStream_ContractTest


class SpringJdbcEventStore_as_EventStream_Test extends EventStream_ContractTest {

    SpringJdbcEventStore eventStore
    Connection sentinelConnection


    @Override
    EventStream getEventStream() {
        return eventStore
    }

    @Before
    public void setUp() {
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

    @Override
    void setStreamHistory(List<EventEnvelope> history) {
        eventStore.saveEnvelopes(history)
    }

}
