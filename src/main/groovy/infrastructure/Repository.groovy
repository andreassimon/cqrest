package infrastructure

import org.springframework.jdbc.core.JdbcTemplate

class Repository {

    JdbcTemplate jdbcTemplate

    def getEventsFor(Class clazz, entityId) {
        return jdbcTemplate.queryForList('SELECT * from events;')
    }

}
