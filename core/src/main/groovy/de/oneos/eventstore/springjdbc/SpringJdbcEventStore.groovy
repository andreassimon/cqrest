package de.oneos.eventstore.springjdbc

import java.sql.*
import javax.sql.*
import groovy.json.*

import org.apache.commons.logging.*

import de.oneos.eventsourcing.*
import de.oneos.eventstore.*

import org.springframework.dao.*
import org.springframework.jdbc.core.*
import org.springframework.jdbc.datasource.*

import org.springframework.transaction.support.*


class SpringJdbcEventStore implements EventStore {
    static Log log = LogFactory.getLog(SpringJdbcEventStore)


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
    TransactionTemplate transactionTemplate
    protected List<EventPublisher> publishers = []

    void setDataSource(DataSource dataSource) {
        jdbcTemplate = new JdbcTemplate(dataSource)
        transactionTemplate =
            new TransactionTemplate(
                new DataSourceTransactionManager(dataSource))
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
    void setPublishers(List<EventPublisher> eventPublishers) {
        this.publishers = eventPublishers
    }

    @Override
    void inUnitOfWork(Closure closure) {
        UnitOfWork unitOfWork = createUnitOfWork()
        closure.delegate = unitOfWork
        closure()
        commit(unitOfWork)
    }

    @Override
    UnitOfWork createUnitOfWork() {
        return new UnitOfWork()
    }

    @Override
    void commit(UnitOfWork unitOfWork) throws IllegalArgumentException, EventCollisionOccurred {
        doInTransaction {
            unitOfWork.eachEventEnvelope saveEventEnvelope
        }
        unitOfWork.eachEventEnvelope { publish(it) }
    }

    protected publish(envelope) {
        publishers.each { publisher ->
            try {
                publisher.publish(envelope)
            } catch (Throwable e) {
                log.warn("Exception during publishing $envelope to $publisher", e)
            }
        }
    }

    protected doInTransaction(Closure<Void> callback) {
        transactionTemplate.execute(
            [doInTransaction: callback] as TransactionCallback
        )
    }

    protected Closure<Void> saveEventEnvelope = { EventEnvelope eventEnvelope ->
        AssertEventEnvelope.isValid(eventEnvelope)

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
        jdbcTemplate.query(FIND_AGGREGATE_EVENTS,
            eventEnvelopeMapper(eventFactory),
            applicationName,
            boundedContextName,
            aggregateName,
            aggregateId
        )
    }

    protected eventEnvelopeMapper(Closure<Event> eventFactory) {
        [mapRow: { ResultSet rs, int rowNum ->
            new EventEnvelope(
                rs.getString('application_name'),
                rs.getString('bounded_context_name'),
                rs.getString('aggregate_name'),
                rs.getObject('aggregate_id'),
                eventFactory(
                    rs.getString('event_name'),
                    json.parseText(rs.getString('attributes'))
                ),
                rs.getInt('sequence_number'),
                rs.getTimestamp('timestamp')
            )
        }
        ] as RowMapper<EventEnvelope>
    }

}
