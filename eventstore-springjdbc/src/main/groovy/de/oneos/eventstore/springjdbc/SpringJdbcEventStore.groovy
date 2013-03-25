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
    aggregate_id,
    sequence_number,
    application_name,
    bounded_context_name,
    aggregate_name,
    event_name,
    attributes,
    timestamp
) VALUES (?,?,?,?,?,?,?,?);\
""".toString()

    static String FIND_AGGREGATE_EVENTS = """\
SELECT *
FROM ${TABLE_NAME}
WHERE aggregate_id = ?;\
""".toString()

    protected eventEnvelopeMapper = [
        mapRow: { ResultSet rs, int rowNum ->
            new EventEnvelope(
                rs.getString('application_name'),
                rs.getString('bounded_context_name'),
                rs.getString('aggregate_name'),
                (UUID)rs.getObject('aggregate_id'),
                buildEvent(
                    rs.getString('event_name'),
                    json.parseText(rs.getString('attributes'))
                ),
                rs.getInt('sequence_number'),
                rs.getTimestamp('timestamp')
            )
        }
    ] as RowMapper<EventEnvelope>


    JsonSlurper json = new JsonSlurper()

    String application
    String boundedContext

    JdbcOperations jdbcTemplate
    TransactionTemplate transactionTemplate
    EventClassResolver eventClassResolver
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
CREATE TABLE IF NOT EXISTS ${TABLE_NAME} (
    aggregate_id         UUID         NOT NULL,
    sequence_number      INTEGER      NOT NULL,
    application_name     VARCHAR(255) NOT NULL,
    bounded_context_name VARCHAR(255) NOT NULL,
    aggregate_name       VARCHAR(255) NOT NULL,
    event_name           VARCHAR(255) NOT NULL,
    attributes           TEXT         NOT NULL,
    timestamp            TIMESTAMP    NOT NULL,
    CONSTRAINT unambiguous_event_sequence UNIQUE (
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
            jdbcTemplate.execute("CREATE INDEX IF NOT EXISTS idx_${TABLE_NAME}_${columnName} ON ${TABLE_NAME} (${columnName});")
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
        return new UnitOfWork(this, application, boundedContext)
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
                eventEnvelope.aggregateId,
                eventEnvelope.sequenceNumber,
                eventEnvelope.applicationName,
                eventEnvelope.boundedContextName,
                eventEnvelope.aggregateName,
                eventEnvelope.eventName,
                eventEnvelope.serializedEvent,
                eventEnvelope.timestamp
            ); return
        } catch (DuplicateKeyException e) {
            throw new EventCollisionOccurred(eventEnvelope, e)
        }
    }

    @Override
    List<EventEnvelope> loadEventEnvelopes(UUID aggregateId) {
        jdbcTemplate.query(FIND_AGGREGATE_EVENTS,
            eventEnvelopeMapper,
            aggregateId
        )
    }

    protected Event buildEvent(String eventName, Map eventAttributes) {
        // TODO How to deal with events that no class can be found for, e.g. inner classes?
        Class<? extends Event> eventClass = eventClassResolver.resolveEvent(eventName)
        def deserializedEvent = eventClass.newInstance()
        eventAttributes.each { propertyName, rawValue ->
            deserializedEvent[propertyName] = rawValue
        }
        return deserializedEvent
    }


}
