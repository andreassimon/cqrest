package de.oneos.cqrs.readmodels.springjdbc

import de.oneos.cqrs.readmodels.Models
import de.oneos.cqrs.readmodels.Selection
import org.springframework.jdbc.core.JdbcOperations
import org.springframework.jdbc.core.JdbcTemplate

import javax.sql.DataSource

class SpringJdbcModels implements Models {
    Class readModelClass
    JdbcOperations jdbcTemplate

    Map update = [
        sql: '',
        args: null
    ]

    void setDataSource(DataSource dataSource) {
        jdbcTemplate = new JdbcTemplate(dataSource)
    }

    void add(newModelInstance) {
        this.update = [
            sql: "INSERT INTO ${tableName(newModelInstance)}(${modelAttributeNames(newModelInstance).join(', ')}) VALUES (${modelAttributeNames(newModelInstance).collect {'?'}.join(', ')});",
            args: modelAttributes(newModelInstance)
        ]
    }

    Selection findAll(Closure filter) {
        new SpringJdbcSelection(this, filter)
    }

    void removeAll(Closure filter) {
        new SpringJdbcSelection(this, filter).delete()
    }

    void materialize() {
        jdbcTemplate.update(update.sql, update.args)
    }

    Object[] modelAttributes(modelInstance) {
        persistedProperties(modelInstance).collect { it.value }
    }

    private Map persistedProperties(modelInstance) {
        modelInstance.properties.findAll { !['class'].contains(it.key) }
    }

    List<String> modelAttributeNames(modelInstance) {
        persistedProperties(modelInstance).collect { toSnakeCase(it.key) }
    }

    String tableName(Class modelClass) {
        toSnakeCase(modelClass.simpleName)
    }

    String tableName(modelInstance) {
        tableName(modelInstance.class)
    }

    static String toSnakeCase(String camelCaseName) {
        camelCaseName.replaceAll(/(.)([A-Z])/, '$1_$2').toLowerCase()
    }

    @Override
    String toString() {
        "${this.class.simpleName}${readModelClass ? "/${tableName(readModelClass)}" : ''}"
    }
}
