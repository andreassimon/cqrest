package de.oneos.cqrs.readmodels

import org.springframework.jdbc.core.JdbcTemplate

class ReadModelRepository {
    JdbcTemplate jdbcTemplate

    ReadModelRepository() { }

    ReadModelRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate
    }

    def getAll(Class<?> readModelClass) {
        final records = jdbcTemplate.queryForList("SELECT * from ${readModelClass.simpleName}")
        records.collect { map ->
            def newEntitiy = readModelClass.newInstance()
            map.each { key, value ->
                newEntitiy[key.toLowerCase()] = value
            }
            newEntitiy
        }
    }
}

