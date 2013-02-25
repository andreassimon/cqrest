package de.oneos.eventstore.springjdbc

class Assert {

    static envelopePropertyIsNotEmpty(bean, String propertyName) {
        Assert.envelopePropertyIsNotNull(bean, propertyName)
        if(bean[propertyName].empty) {
            throw new IllegalArgumentException("The envelope's $propertyName must not be empty")
        }
    }

    static envelopePropertyIsNotNull(bean, String propertyName) {
        if (bean[propertyName] == null) {
            throw new IllegalArgumentException("The envelope's $propertyName must not be null")
        }
    }

}
