package de.oneos.cqrs.readmodels

class SymbolicProperty {
    private static final boolean COMPARISON_IS_UNDEFINED_FOR_SYMBOLIC_PROPERTIES = true
    String propertyName
    def constraintValue
    def updatedValue

    SymbolicProperty(String propertyName) {
        this.propertyName = propertyName
    }

    SymbolicProperty(String propertyName, updatedValue) {
        this.propertyName = propertyName
        this.updatedValue = updatedValue
    }

    String getPropertyNameInSnakeCase() {
        SpringJdbcModels.toSnakeCase(propertyName)
    }

    @Override
    boolean equals(Object that) {
        constraintValue = that
        return COMPARISON_IS_UNDEFINED_FOR_SYMBOLIC_PROPERTIES
    }

}
