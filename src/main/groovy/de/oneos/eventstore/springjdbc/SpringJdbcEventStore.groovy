package de.oneos.eventstore.springjdbc

import javax.sql.*
import groovy.json.*

import de.oneos.eventsourcing.*
import de.oneos.eventstore.*

import org.springframework.dao.*
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
    application_name     VARCHAR(255) NOT NULL,
    bounded_context_name VARCHAR(255) NOT NULL,
    aggregate_name       VARCHAR(255) NOT NULL,
    aggregate_id         UUID         NOT NULL,
    sequence_number      INTEGER      NOT NULL,
    event_name           VARCHAR(255) NOT NULL,
    attributes           TEXT         NOT NULL,
    timestamp            TIMESTAMP    NOT NULL,
    CONSTRAINT unambiguous_event_sequence UNIQUE (
        application_name,
        bounded_context_name,
        aggregate_name,
        aggregate_id,
        sequence_number
    )
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
        AssertEventEnvelope.notEmpty(eventEnvelope, 'applicationName')
        AssertEventEnvelope.notEmpty(eventEnvelope, 'boundedContextName')
        AssertEventEnvelope.notEmpty(eventEnvelope, 'aggregateName')
        AssertEventEnvelope.notNull(eventEnvelope, 'aggregateId')
        AssertEventEnvelope.notNull(eventEnvelope, 'sequenceNumber')
        AssertEventEnvelope.notEmpty(eventEnvelope, 'eventName')
        AssertEventEnvelope.notNull(eventEnvelope, 'timestamp')

        try {
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
        } catch (DuplicateKeyException e) {
            throw new EventCollisionOccurred(eventEnvelope, e)
        }
    }

    @Override
    List<EventEnvelope> loadEventEnvelopes(String applicationName, String boundedContextName, String aggregateName, UUID aggregateId, Closure<Event> eventFactory) {
        final records = jdbcTemplate.queryForList(FIND_AGGREGATE_EVENTS,
            applicationName,
            boundedContextName,
            aggregateName,
            aggregateId
        )

        records.collect { record ->
            new EventEnvelope(
                record['application_name'],
                record['bounded_context_name'],
                record['aggregate_name'],
                record['aggregate_id'],
                eventFactory(
                    record['event_name'],
                    json.parseText(record['attributes'])
                ),
                record['sequence_number'],
                record['timestamp']
            )
        }
    }

}
