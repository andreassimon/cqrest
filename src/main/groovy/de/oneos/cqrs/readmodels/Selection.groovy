package de.oneos.cqrs.readmodels

class Selection {
    SpringJdbcModels springJdbcModels
    Closure filter
    Class filteredModelClass
    def constrainedProperties = []

    Selection(SpringJdbcModels springJdbcModels, Closure filter) {
        this.springJdbcModels = springJdbcModels
        this.filter = filter

        filteredModelClass = filter.parameterTypes[0]

        def surrogateModel = filteredModelClass.newInstance()
        surrogateModel.metaClass.getProperty = { String name ->
            def symbolicProperty = new SymbolicProperty(name)
            constrainedProperties << symbolicProperty
            return symbolicProperty
        }
        filter(surrogateModel)
    }

    void delete() {
        springJdbcModels.update = [
            sql: "DELETE FROM ${tableName()} WHERE ${constrainedPropertyNames().join(' AND ')}",
            args: constraintValues()
        ]
    }

    private Object[] constraintValues() {
        constrainedProperties.collect { it.constraintValue }
    }

    private constrainedPropertyNames() {
        constrainedProperties.collect { "${it.propertyNameInSnakeCase} = ?"}
    }

    private tableName() {
        SpringJdbcModels.toSnakeCase(filteredModelClass.simpleName)
    }
}
