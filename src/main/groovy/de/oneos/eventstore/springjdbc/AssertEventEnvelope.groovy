package de.oneos.eventstore.springjdbc

class AssertEventEnvelope {

    static notEmpty(bean, String propertyName) {
        AssertEventEnvelope.notNull(bean, propertyName)
        if(bean[propertyName].empty) {
            throw new IllegalArgumentException("The envelope's $propertyName must not be empty")
        }
    }

    static notNull(bean, String propertyName) {
        if (bean[propertyName] == null) {
            throw new IllegalArgumentException("The envelope's $propertyName must not be null")
        }
    }

}
