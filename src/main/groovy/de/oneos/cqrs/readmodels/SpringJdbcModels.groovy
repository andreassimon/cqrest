package de.oneos.cqrs.readmodels

import org.springframework.jdbc.core.JdbcOperations

class SpringJdbcModels implements Models {
    JdbcOperations jdbcTemplate

    private newModelInstance

    void add(newModelInstance) {
        this.newModelInstance = newModelInstance
    }

    void materialize() {
        jdbcTemplate.update("INSERT INTO ${tableName(newModelInstance)}(${modelAttributeNames(newModelInstance).join(', ')}) VALUES (${modelAttributeNames(newModelInstance).collect {'?'}.join(', ')});", modelAttributes(newModelInstance))
    }

    Object[] modelAttributes(modelInstance) {
        persistedProperties(modelInstance).collect { property ->
            property.value
        }
    }

    private Map persistedProperties(modelInstance) {
        modelInstance.properties.findAll { !['class'].contains(it.key) }
    }

    List<String> modelAttributeNames(modelInstance) {
        persistedProperties(modelInstance).collect { it.key }
    }

    String tableName(modelInstance) {
        modelInstance.class.simpleName.replaceAll(/(.)([A-Z])/, '$1_$2').toLowerCase()
    }
}
