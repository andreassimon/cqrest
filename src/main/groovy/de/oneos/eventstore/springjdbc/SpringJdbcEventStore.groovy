package de.oneos.eventstore.springjdbc

import javax.sql.*
import groovy.json.*

import de.oneos.eventsourcing.*
import de.oneos.eventstore.*

import org.springframework.jdbc.core.*


class SpringJdbcEventStore implements EventStore {

    static String TABLE_NAME = 'events'
    static String INSERT_EVENT = """\
INSERT INTO ${TABLE_NAME} (
    application_name,
    bounded_context_name,
    aggregate_name,
    aggregate_id,
    sequence_number,
    event_name,
    attributes,
    timestamp
) VALUES (?,?,?,?,?,?,?,?);\
""".toString()

    static String FIND_AGGREGATE_EVENTS = """\
SELECT *
FROM ${TABLE_NAME}
WHERE application_name = ? AND
      bounded_context_name = ? AND
      aggregate_name = ? AND
      aggregate_id = ?;\
""".toString()


    JsonSlurper json = new JsonSlurper()
    JdbcOperations jdbcTemplate

    void setDataSource(DataSource dataSource) {
        jdbcTemplate = new JdbcTemplate(dataSource)
    }


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

        ['application_name',
         'bounded_context_name',
         'aggregate_name',
         'aggregate_id',
         'sequence_number',
         'event_name',
         'timestamp'
        ].each { columnName ->
            jdbcTemplate.execute("CREATE INDEX idx_${TABLE_NAME}_${columnName} ON ${TABLE_NAME} (${columnName});")
        }
    }

    @Override
    UnitOfWork createUnitOfWork() {
        return new UnitOfWork()
    }

    @Override
    void save(EventEnvelope eventEnvelope) {
        jdbcTemplate.update(INSERT_EVENT,
            eventEnvelope.applicationName,
            eventEnvelope.boundedContextName,
            eventEnvelope.aggregateName,
            eventEnvelope.aggregateId,
            eventEnvelope.sequenceNumber,
            eventEnvelope.eventName,
            eventEnvelope.serializedEvent,
            eventEnvelope.timestamp
        )
    }

    @Override
    List loadEvents(String applicationName, String boundedContextName, String aggregateName, UUID aggregateId, Closure<Object> eventFactory) {
        final records = jdbcTemplate.queryForList(FIND_AGGREGATE_EVENTS,
            applicationName,
            boundedContextName,
            aggregateName,
            aggregateId
        )

        records.collect { record ->
            eventFactory(
                record['event_name'],
                json.parseText(record['attributes'])
            )
        }
    }

}
