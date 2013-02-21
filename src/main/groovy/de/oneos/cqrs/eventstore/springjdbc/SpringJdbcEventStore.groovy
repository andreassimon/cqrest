package de.oneos.cqrs.eventstore.springjdbc

import de.oneos.cqrs.eventstore.EventStore
import de.oneos.cqrs.eventstore.StaleStateException
import de.oneos.cqrs.eventstore.UnitOfWork
import de.oneos.cqrs.eventstore.UnknownAggregate
import domain.events.EventEnvelope
import groovy.json.JsonSlurper
import infrastructure.persistence.PersistentEventPublisher
import org.springframework.dao.DuplicateKeyException
import org.springframework.jdbc.core.JdbcTemplate

class SpringJdbcEventStore implements EventStore {
    JdbcTemplate jdbcTemplate

    Map<Integer, UnitOfWork> unitsOfWork = Collections.synchronizedMap([:])

    void createTable() {
        jdbcTemplate.execute("DROP TABLE IF EXISTS events;")
        jdbcTemplate.execute('''\
CREATE TABLE events (
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
);
''')
        for (columnName in ['application_name', 'bounded_context_name', 'aggregate_name', 'aggregate_id', 'event_name', 'timestamp']) {
            jdbcTemplate.execute("CREATE INDEX idx_events_${columnName} ON events (${columnName});")
        }
    }

    def save(EventEnvelope eventEnvelope) {
        jdbcTemplate.update(
            "INSERT INTO Events (application_name, bounded_context_name, aggregate_name, aggregate_id, sequence_number, event_name, attributes, timestamp) VALUES (?,?,?,?,?,?,?,?);",
                eventEnvelope.applicationName,
                eventEnvelope.boundedContextName,
                eventEnvelope.aggregateName,
                eventEnvelope.aggregateId,
                eventEnvelope.sequenceNumber ?: 0,
                eventEnvelope.eventName,
                eventEnvelope.serializedEvent,
                eventEnvelope.timestamp)
    }

    @Override
    def getAggregate(String applicationName, String boundedContextName, String aggregateName, Class aggregateClass, UUID aggregateId, String eventPackageName) {
        def aggregateEvents = getEventsFor(applicationName, boundedContextName, aggregateName, aggregateId, eventPackageName)
        if(aggregateEvents.empty) { throw new UnknownAggregate(aggregateClass, aggregateId) }

        UnitOfWork unitOfWork = new UnitOfWork(new PersistentEventPublisher(eventStore: this))
        unitOfWork.applicationName = applicationName
        unitOfWork.boundedContextName = boundedContextName
        unitOfWork.aggregateName = aggregateName
        unitOfWork.aggregateId = aggregateId

        def aggregateInstance = aggregateClass.newInstance()
        aggregateInstance.metaClass = expando(aggregateClass)
        aggregateInstance.unitOfWork = unitOfWork

        return aggregateEvents.inject(aggregateInstance) { aggregate, event ->
            event.applyTo(aggregate)
        }
    }

    private expando(Class aggregateClass) {
        ExpandoMetaClass expandoAggregateClass = new ExpandoMetaClass(aggregateClass)
        expandoAggregateClass.setUnitOfWork = { unitOfWork ->
            unitsOfWork[System.identityHashCode(delegate)] = unitOfWork
        }
        expandoAggregateClass.getUnitOfWork = {->
            unitsOfWork[System.identityHashCode(delegate)]
        }
        expandoAggregateClass.publishEvent = { event ->
            delegate.unitOfWork.publish(event)
        }
        expandoAggregateClass.flush = { ->
            try {
                delegate.unitOfWork.flush()
            } catch(DuplicateKeyException e) {
                throw new StaleStateException(e)
            }
        }
        expandoAggregateClass.initialize()
        expandoAggregateClass
    }

    @Override
    List getEventsFor(String applicationName, String boundedContextName, String aggregateName, UUID aggregateId, String eventPackageName) {
        final records = jdbcTemplate.queryForList('SELECT * from events WHERE application_name = ? AND bounded_context_name = ? AND aggregate_name = ? AND aggregate_id = ?;', applicationName, boundedContextName, aggregateName, aggregateId)
        records.collect { record ->
            def simpleEventClassName = record['event_name'].replaceAll(' ', '_')
            def fullEventClassName = eventPackageName + simpleEventClassName

            final eventAttributesJson = record['ATTRIBUTES']
            LinkedHashMap<String, String> eventAttributesMap = new JsonSlurper().parseText(eventAttributesJson)
            this.class.classLoader.loadClass(fullEventClassName).newInstance(eventAttributesMap)
        }
    }

}
