package de.oneos.cqrs.readmodels

class UpdatedProperty {

    String propertyName
    def updatedValue

    UpdatedProperty(String propertyName, updatedValue) {
        this.propertyName = propertyName
        this.updatedValue = updatedValue
    }

    String getPropertyNameInSnakeCase() {
        SpringJdbcModels.toSnakeCase(propertyName)
    }

}
