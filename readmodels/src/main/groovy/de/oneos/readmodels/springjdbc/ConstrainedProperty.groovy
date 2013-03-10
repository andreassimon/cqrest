package de.oneos.readmodels.springjdbc

protected class ConstrainedProperty {
    private static final boolean COMPARISON_IS_UNDEFINED_FOR_SYMBOLIC_PROPERTIES = true

    String propertyName
    def constraintValue

    ConstrainedProperty(String propertyName) {
        this.propertyName = propertyName
    }

    String getPropertyNameInSnakeCase() {
        SpringJdbcReadmodels.toSnakeCase(propertyName)
    }

    @Override
    boolean equals(Object that) {
        constraintValue = that
        return COMPARISON_IS_UNDEFINED_FOR_SYMBOLIC_PROPERTIES
    }

}
