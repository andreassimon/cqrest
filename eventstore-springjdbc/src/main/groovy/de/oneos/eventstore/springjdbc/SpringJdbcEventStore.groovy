package de.oneos.eventstore.springjdbc

import de.oneos.eventsourcing.*

import java.sql.*
import javax.sql.*

import org.apache.commons.logging.*

import de.oneos.eventstore.*

import org.springframework.dao.*
import org.springframework.jdbc.core.*
import org.springframework.jdbc.core.namedparam.*
import org.springframework.jdbc.datasource.*

import org.springframework.transaction.support.*


class SpringJdbcEventStore implements EventStore, EventStream {
    static Log log = LogFactory.getLog(SpringJdbcEventStore)

    public static final boolean CREATE_TABLE = true
    public static final boolean DONT_CREATE_TABLE = !CREATE_TABLE


    static String TABLE_NAME = 'events'
    static String INSERT_EVENT = """\
INSERT INTO ${TABLE_NAME} (
    aggregate_id,
    sequence_number,
    correlation_id,
    application_name,
    bounded_context_name,
    aggregate_name,
    event_name,
    attributes,
    user,
    timestamp
) VALUES (?,?,?,?,?,?,?,?,?,?);\
""".toString()

    protected eventEnvelopeMapper = new EventEnvelopeRowMapper()

    JdbcOperations jdbcTemplate
    NamedParameterJdbcTemplate namedParameterJdbcTemplate
    TransactionTemplate transactionTemplate
    protected List<EventConsumer> processors = []
    protected EventBus eventBus

    SpringJdbcEventStore(DataSource dataSource, boolean createTable) {
        this(dataSource, new StubEventBus(), createTable)
    }

    SpringJdbcEventStore(DataSource dataSource, EventBus eventBus, boolean createTable) {
        setDataSource(dataSource)
        this.eventBus = eventBus
        if(createTable) {
            this.createTable()
        }
    }

    void setDataSource(DataSource dataSource) {
        jdbcTemplate = new JdbcTemplate(dataSource)
        namedParameterJdbcTemplate = new NamedParameterJdbcTemplate(dataSource)
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
        jdbcTemplate.execute("""\
ALTER TABLE ${TABLE_NAME} ADD COLUMN IF NOT EXISTS correlation_id UUID BEFORE application_name;\
""")

        jdbcTemplate.execute("""\
ALTER TABLE ${TABLE_NAME} ADD COLUMN IF NOT EXISTS user VARCHAR(255) BEFORE timestamp;\
""")

        ['aggregate_id',
         'sequence_number',
         'correlation_id',
         'application_name',
         'bounded_context_name',
         'aggregate_name',
         'event_name',
         'user',
         'timestamp'
        ].each { columnName ->
            jdbcTemplate.execute("CREATE INDEX IF NOT EXISTS idx_${TABLE_NAME}_${columnName} ON ${TABLE_NAME} (${columnName});")
        }
    }



    @Override
    void subscribeTo(Map<String, ?> criteria, EventConsumer eventConsumer) {
        assert null != eventConsumer
        processors.add(eventConsumer)
    }

    @Override
    public Correlation inUnitOfWork(String application, String boundedContext, UUID correlationId, String user, Closure closure) {
        Correlation correlation = new Correlation(correlationId)
        eventBus.subscribeCorrelation(correlation)
        UnitOfWork unitOfWork = createUnitOfWork(application, boundedContext, correlationId, user)
        closure.delegate = unitOfWork
        if(closure.maximumNumberOfParameters == 0) {
            closure.call()
        } else {
            closure.call(unitOfWork)
        }
        commit(unitOfWork)
        return correlation
    }

    @Override
    UnitOfWork createUnitOfWork(String application, String boundedContext, UUID correlationId, String user) {
        return new UnitOfWork(this, application, boundedContext, correlationId, user)
    }

    @Override
    void commit(UnitOfWork unitOfWork) throws IllegalArgumentException, EventCollisionOccurred {
        saveEnvelopes(unitOfWork.getEventEnvelopes())
        unitOfWork.flush()
    }

    public void saveEnvelopes(List<EventEnvelope> envelopes) {
        doInTransaction {
            envelopes.each saveEventEnvelope
        }
        envelopes.each process
    }

    protected Closure<?> process = { EventEnvelope envelope ->
        processors.each { processor ->
            try {
                processor.process(envelope)
            } catch (Throwable e) {
                log.warn("Exception during processing $envelope by $processor", e)
            }
        }
    }

    protected doInTransaction(Closure<?> callback) {
        transactionTemplate.execute(callback as TransactionCallback)
    }

    protected Closure<Void> saveEventEnvelope = { EventEnvelope eventEnvelope ->
        AssertEventEnvelope.isValid(eventEnvelope)

        try {
            jdbcTemplate.update(INSERT_EVENT,
                eventEnvelope.aggregateId,
                eventEnvelope.sequenceNumber,
                eventEnvelope.correlationId,
                eventEnvelope.applicationName,
                eventEnvelope.boundedContextName,
                eventEnvelope.aggregateName,
                eventEnvelope.eventName,
                eventEnvelope.serializedEvent,
                eventEnvelope.user,
                eventEnvelope.timestamp
            ); return
        } catch (DuplicateKeyException e) {
            throw new EventCollisionOccurred(eventEnvelope, e)
        }
    }

    @Override
    List<EventEnvelope> findAll(Map<String, ?> criteria) {
        namedParameterJdbcTemplate.query(
            queryExpression(criteria),
            criteria,
            eventEnvelopeMapper
        )
    }

    protected String queryExpression(Map<String, ?> criteria) {
        """\
SELECT *
FROM ${TABLE_NAME}
${whereClause(criteria)}
ORDER BY aggregate_id, sequence_number;\
""".toString()
    }

    protected final static Map<String, String> COLUMN_NAME = [
        boundedContextName: 'bounded_context_name',
        aggregateName: 'aggregate_name',
        aggregateId: 'aggregate_id',
        eventName:   'event_name'
    ]

    protected whereClause(Map<String, ?> criteria) {
        if (criteria.isEmpty()) { return '' }
        'WHERE ' + criteria.collect { attribute, values -> condition(attribute, values) }.join(' AND ')
    }

    protected condition(String attribute, values) {
        switch(values.getClass()) {
            case [String, UUID]:
                return "${COLUMN_NAME[attribute]} = :$attribute".toString()
            case List:
                return "event_name IN (:$attribute)".toString()
            default:
                throw new RuntimeException("Cannot transform ($attribute, ${values.getClass().simpleName}<$values>) to WHERE clause")
        }
    }

    protected def buildEvent(String eventName, Map eventAttributes) {
        return [
            eventName: eventName,
            eventAttributes: eventAttributes
        ]
    }

    @Override
    void withEventEnvelopes(Map<String, ?> criteria, Closure block) {
        namedParameterJdbcTemplate.query(
            queryExpression(criteria),
            criteria,
            { ResultSet resultSet ->
                block.call(
                    eventEnvelopeMapper.mapRow(resultSet, resultSet.row)
                )
            } as RowCallbackHandler
        )
    }

    @Override
    org.cqrest.reactive.Observable<EventEnvelope> observe(Map<String, ?> criteria = [:]) {
        throw new RuntimeException('Not implemented')
    }

}
