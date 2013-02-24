package de.oneos.eventstore.springjdbc

import de.oneos.eventstore.*

import org.springframework.jdbc.core.JdbcOperations


class SpringJdbcEventStore implements EventStore {

    static String TABLE_NAME = 'events'


    JdbcOperations jdbcTemplate

    void dropTable() {
        jdbcTemplate.execute("DROP TABLE IF EXISTS ${TABLE_NAME};")
    }

    void createTable() {
        jdbcTemplate.execute("""\
CREATE TABLE ${TABLE_NAME} (
    application_name     VARCHAR(255),
    bounded_context_name VARCHAR(255),
    aggregate_name       VARCHAR(255),
    aggregate_id         UUID,
    sequence_number      INTEGER,
    event_name           VARCHAR(255),
    attributes           TEXT,
    timestamp            TIMESTAMP
);\
""")

        ['application_name', 'bounded_context_name', 'aggregate_name', 'aggregate_id', 'event_name', 'timestamp'].each { columnName ->
            jdbcTemplate.execute("CREATE INDEX idx_events_${columnName} ON events (${columnName});")
        }
    }

    @Override
    UnitOfWork createUnitOfWork() {
        return new UnitOfWork()
    }

}
