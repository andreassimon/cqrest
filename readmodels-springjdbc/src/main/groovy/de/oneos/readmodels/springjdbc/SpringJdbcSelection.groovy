package de.oneos.readmodels.springjdbc

import de.oneos.readmodels.*


protected class SpringJdbcSelection implements Selection {
    SpringJdbcReadmodels springJdbcModels
    Closure filter
    Class filteredModelClass
    List constrainedProperties = []
    List updatedProperties = []

    SpringJdbcSelection(SpringJdbcReadmodels springJdbcModels, Closure filter) {
        this.springJdbcModels = springJdbcModels
        this.filter = filter

        filteredModelClass = filter.parameterTypes[0]
        if(filteredModelClass == Object) {
            throw new RuntimeException("The parameter type for closure ${filter.toString()} must be specified!")
        }

        filter(surrogateFilterModel())
    }

    private surrogateFilterModel() {
        def surrogateFilterModel = filteredModelClass.newInstance()
        surrogateFilterModel.metaClass.getProperty = { String name ->
            def constrainedProperty = new ConstrainedProperty(name)
            constrainedProperties << constrainedProperty
            return constrainedProperty
        }
        surrogateFilterModel
    }

    void delete() {
        springJdbcModels.update = [
            sql: "DELETE FROM ${tableName()} WHERE ${constrainedPropertyNames().join(' AND ')}",
            args: constraintValues()
        ]
    }

    void each(Closure update) {
        update(surrogateUpdateModel())
        springJdbcModels.update = [
            sql: "UPDATE ${tableName()} SET ${updatedPropertyNames().join(', ')} WHERE ${constrainedPropertyNames().join(' AND ')};",
            args: updatedValues() + constraintValues()
        ]
    }

    private surrogateUpdateModel() {
        def surrogateUpdateModel = filteredModelClass.newInstance()
        surrogateUpdateModel.metaClass.setProperty = { String name, newValue ->
            updatedProperties << new UpdatedProperty(name, newValue)
        }
        surrogateUpdateModel
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
        SpringJdbcReadmodels.toSnakeCase(filteredModelClass.simpleName)
    }
}
