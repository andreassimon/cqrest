package infrastructure

import groovy.json.JsonSlurper
import org.springframework.jdbc.core.JdbcTemplate

class JdbcRepository implements Repository {

    JdbcTemplate jdbcTemplate
    private JsonSlurper jsonSlurper = new JsonSlurper()

    // TODO Unterschiedliche DBMS gehen unterschiedlich mit der Gross- / Kleinschreibung von
    //      Attributen um. Das macht das Mapping problematisch.
    @Override
    def getEventsFor(Class aggregateClass, UUID aggregateId) {
        final records = jdbcTemplate.queryForList('SELECT * from events WHERE AggregateClassName = ? AND AggregateId = ?;', aggregateClass.canonicalName, aggregateId)
        records.collect { record ->
            def simpleEventClassName = record['EVENTNAME'].replaceAll(' ', '_')
            def fullEventClassName = 'domain.events.' + simpleEventClassName

            final eventAttributesJson = record['ATTRIBUTES']
            LinkedHashMap<String, String> eventAttributesMap = new JsonSlurper().parseText(eventAttributesJson)
            println eventAttributesMap
            this.class.classLoader.loadClass(fullEventClassName).newInstance(eventAttributesMap[record['EVENTNAME']])
        }
    }

}
