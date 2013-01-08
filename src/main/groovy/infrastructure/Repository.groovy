package infrastructure

import groovy.json.JsonSlurper
import org.springframework.jdbc.core.JdbcTemplate

class Repository {

    JdbcTemplate jdbcTemplate

    // TODO Unterschiedliche DBMS gehen unterschiedlich mit der Gross- / Kleinschreibung von
    //      Attributen um. Das macht das Mapping problematisch.
    def getEventsFor(Class aggregateClass, entityId) {
        final records = jdbcTemplate.queryForList('SELECT * from events WHERE AggregateClassName = ? AND AggregateId = ?;', aggregateClass.canonicalName, entityId)
        records.collect { record ->
            def simpleEventClassName = record['EVENTNAME'].replaceAll(' ', '_')
            def fullEventClassName = 'domain.events.' + simpleEventClassName

            final eventAttributesJson = record['ATTRIBUTES']
            def eventAttributesMap = new JsonSlurper().parseText(eventAttributesJson)
            this.class.classLoader.loadClass(fullEventClassName).newInstance(eventAttributesMap[0])
        }
    }

}
