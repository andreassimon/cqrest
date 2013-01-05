package readmodels

import org.springframework.jdbc.core.JdbcTemplate

class ReadModelRepository {
    JdbcTemplate jdbcTemplate

    ReadModelRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate
    }

    def getAll(Class<?> readModelClass) {
        final result = jdbcTemplate.queryForList("SELECT * from ${readModelClass.simpleName}")
        result.collect { map ->
            def newEntitiy = readModelClass.newInstance()
            map.each { key, value ->
                newEntitiy[key] = value
            }
            newEntitiy
        }
    }
}

