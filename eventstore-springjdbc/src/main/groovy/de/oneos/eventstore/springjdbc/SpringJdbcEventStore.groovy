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


    static String INSERT_EVENT = """\
INSERT INTO ${Schema.TABLE_NAME} (
    ${Schema.AGGREGATE_ID},
    ${Schema.SEQUENCE_NUMBER},
    ${Schema.CORRELATION_ID},
    ${Schema.APPLICATION_NAME},
    ${Schema.BOUNDED_CONTEXT_NAME},
    ${Schema.AGGREGATE_NAME},
    ${Schema.EVENT_NAME},
    ${Schema.ATTRIBUTES},
    ${Schema.USER},
    ${Schema.TIMESTAMP}
) VALUES (?,?,?,?,?,?,?,?,?,?);\
""".toString()
    public static final String EVENT_STORE_QUERY_EXCEPTION_MSG = 'Exception during query of SpringJdbcEventStore'

    protected eventEnvelopeMapper = new EventEnvelopeRowMapper()

    JdbcOperations jdbcTemplate
    NamedParameterJdbcOperations namedParameterJdbcTemplate
    TransactionTemplate transactionTemplate
    Subscribers subscribers = new Subscribers(log)
    protected EventBus eventBus

    SpringJdbcEventStore(DataSource dataSource, boolean createTable) {
        this(dataSource, defaultEventBus(), createTable)
    }

    public static EventBus defaultEventBus() {
        return new StubEventBus()
    }

    SpringJdbcEventStore(DataSource dataSource, EventBus eventBus, boolean createTable) {
        this(
          new JdbcTemplate(dataSource),
          new NamedParameterJdbcTemplate(dataSource),
          new TransactionTemplate(
            new DataSourceTransactionManager(dataSource)),
          eventBus,
          createTable
        )
    }

    SpringJdbcEventStore(
      JdbcOperations jdbcTemplate,
      NamedParameterJdbcOperations namedParameterJdbcTemplate,
      TransactionTemplate transactionTemplate,
      EventBus eventBus,
      boolean createTable
    ) {
        this.jdbcTemplate = jdbcTemplate
        this.namedParameterJdbcTemplate = namedParameterJdbcTemplate
        this.transactionTemplate = transactionTemplate
        this.eventBus = eventBus
        if(createTable) {
            this.createTable()
        }
    }


    void dropTable() {
        jdbcTemplate.execute("DROP TABLE IF EXISTS ${Schema.TABLE_NAME};")
    }

    void createTable() {
        jdbcTemplate.execute("""\
CREATE TABLE IF NOT EXISTS ${Schema.TABLE_NAME} (
    ${Schema.AGGREGATE_ID}         UUID         NOT NULL,
    ${Schema.SEQUENCE_NUMBER}      INTEGER      NOT NULL,
    ${Schema.APPLICATION_NAME}     VARCHAR(255) NOT NULL,
    ${Schema.BOUNDED_CONTEXT_NAME} VARCHAR(255) NOT NULL,
    ${Schema.AGGREGATE_NAME}       VARCHAR(255) NOT NULL,
    ${Schema.EVENT_NAME}           VARCHAR(255) NOT NULL,
    ${Schema.ATTRIBUTES}           TEXT         NOT NULL,
    ${Schema.TIMESTAMP}            TIMESTAMP    NOT NULL,
    CONSTRAINT unambiguous_event_sequence UNIQUE (
        ${Schema.AGGREGATE_ID},
        ${Schema.SEQUENCE_NUMBER}
    )
);\
""")
        jdbcTemplate.execute("""\
ALTER TABLE ${Schema.TABLE_NAME} ADD COLUMN IF NOT EXISTS ${Schema.CORRELATION_ID} UUID BEFORE ${Schema.APPLICATION_NAME};\
""")

        jdbcTemplate.execute("""\
ALTER TABLE ${Schema.TABLE_NAME} ADD COLUMN IF NOT EXISTS ${Schema.USER} VARCHAR(255) BEFORE ${Schema.TIMESTAMP};\
""")

        [Schema.AGGREGATE_ID,
         Schema.SEQUENCE_NUMBER,
         Schema.CORRELATION_ID,
         Schema.APPLICATION_NAME,
         Schema.BOUNDED_CONTEXT_NAME,
         Schema.AGGREGATE_NAME,
         Schema.EVENT_NAME,
         Schema.USER,
         Schema.TIMESTAMP
        ].each { columnName ->
            jdbcTemplate.execute("CREATE INDEX IF NOT EXISTS idx_${Schema.TABLE_NAME}_${columnName} ON ${Schema.TABLE_NAME} (${columnName});")
        }
    }



    @Override
    void subscribeTo(Map<String, ?> criteria, EventConsumer eventConsumer) {
        assert null != eventConsumer
        subscribers.observable.subscribe(new EventConsumerAdapter(eventConsumer))
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

    protected Closure<?> process = { EventEnvelope eventEnvelope ->
        subscribers.publish(eventEnvelope)
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
            queryExpression.forCriteria(criteria),
            criteria,
            eventEnvelopeMapper
        )
    }

    QueryExpression queryExpression = new QueryExpression()

    @Override
    void withEventEnvelopes(Map<String, ?> criteria, Closure block) throws EventStoreQueryException {
        queryByCriteria(criteria, { ResultSet resultSet ->
            block.call(
              eventEnvelopeMapper.mapRow(resultSet, resultSet.row)
            )
        } as RowCallbackHandler)
    }

    void queryByCriteria(Map<String, ?> criteria, RowCallbackHandler rowCallbackHandler) throws EventStoreQueryException {
        try {
            namedParameterJdbcTemplate.query(
              queryExpression.forCriteria(criteria),
              criteria,
              rowCallbackHandler
            )
        } catch(DataAccessException e) {
            throw createQueryException(e)
        }
    }

    public static EventStoreQueryException createQueryException(DataAccessException e) {
        return new EventStoreQueryException(EVENT_STORE_QUERY_EXCEPTION_MSG, e)
    }

    @Override
    org.cqrest.reactive.Observable<EventEnvelope> observe(Map<String, ?> criteria = [:]) {
        return new org.cqrest.reactive.Observable<EventEnvelope>(
            rx.Observable.concat(
              rx.Observable.create(new QueryOnSubscribe(criteria, this)),
              subscribers.observable.filter(new CriteriaFilter(criteria))
            )
        )
    }

}
