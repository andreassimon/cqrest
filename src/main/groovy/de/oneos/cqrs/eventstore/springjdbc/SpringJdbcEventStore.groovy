package de.oneos.cqrs.eventstore.springjdbc

import org.springframework.jdbc.core.JdbcTemplate

import domain.events.EventEnvelope
import groovy.json.JsonSlurper
import de.oneos.cqrs.eventstore.DefaultEventStore


class SpringJdbcEventStore extends DefaultEventStore {
    JdbcTemplate jdbcTemplate

    void createTable() {
        jdbcTemplate.execute("DROP TABLE IF EXISTS events;")
        jdbcTemplate.execute('''\
CREATE TABLE events (
    application_name     VARCHAR(255) NOT NULL,
    bounded_context_name VARCHAR(255) NOT NULL,
    aggregate_name       VARCHAR(255) NOT NULL,
    aggregate_id         UUID         NOT NULL,
    event_name           VARCHAR(255) NOT NULL,
    attributes           TEXT         NOT NULL,
    timestamp            TIMESTAMP    NOT NULL
);
''')
        for (columnName in ['application_name', 'bounded_context_name', 'aggregate_name', 'aggregate_id', 'event_name', 'timestamp']) {
            jdbcTemplate.execute("CREATE INDEX idx_events_${columnName} ON events (${columnName});")
        }
    }

    def save(EventEnvelope eventEnvelope) {
        jdbcTemplate.update(
            "INSERT INTO Events (application_name, bounded_context_name, aggregate_name, aggregate_id, event_name, attributes, timestamp) VALUES (?,?,?,?,?,?,?);",
                eventEnvelope.applicationName,
                eventEnvelope.boundedContextName,
                eventEnvelope.aggregateName,
                eventEnvelope.aggregateId,
                eventEnvelope.eventName,
                eventEnvelope.serializedEvent,
                eventEnvelope.timestamp)
    }

    // TODO Unterschiedliche DBMS gehen unterschiedlich mit der Gross- / Kleinschreibung von
    //      Attributen um. Das macht das Mapping problematisch.
    @Override
    def getEventsFor(String applicationName, String boundedContextName, String aggregateName, UUID aggregateId, String eventPackageName) {
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
