package de.oneos.cqrs.readmodels

class Selection {
    SpringJdbcModels springJdbcModels
    Closure filter
    Class filteredModelClass
    List constrainedProperties = []
    List updatedProperties = []

    Selection(SpringJdbcModels springJdbcModels, Closure filter) {
        this.springJdbcModels = springJdbcModels
        this.filter = filter

        filteredModelClass = filter.parameterTypes[0]
        if(filteredModelClass == Object) {
            throw new RuntimeException("The parameter type for closure ${filter.toString()} must be specified!")
        }

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

    void each(Closure update) {
        def surrogateModel = filteredModelClass.newInstance()
        surrogateModel.metaClass.setProperty = { String name, newValue ->
            updatedProperties << new SymbolicProperty(name, newValue)
        }
        update(surrogateModel)
        springJdbcModels.update = [
            sql: "UPDATE ${tableName()} SET ${updatedPropertyNames().join(', ')} WHERE ${constrainedPropertyNames().join(' AND ')};",
            args: updatedValues() + constraintValues()
        ]
    }

    Object[] updatedValues() {
        updatedProperties.collect { it.updatedValue }
    }

    private Object[] constraintValues() {
        constrainedProperties.collect { it.constraintValue }
    }

    private updatedPropertyNames() {
        updatedProperties.collect { "${it.propertyNameInSnakeCase} = ?" }
    }

    private constrainedPropertyNames() {
        constrainedProperties.collect { "${it.propertyNameInSnakeCase} = ?" }
    }

    private tableName() {
        SpringJdbcModels.toSnakeCase(filteredModelClass.simpleName)
    }
}
