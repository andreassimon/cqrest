package de.oneos.eventstore.springjdbc

class AssertEventEnvelope {

    static void notEmpty(envelope, String propertyName) {
        AssertEventEnvelope.notNull(envelope, propertyName)
        if(envelope[propertyName].empty) {
            throw new IllegalArgumentException("The envelope's $propertyName must not be empty")
        }
    }

    static void notNull(envelope, String propertyName) {
        if (envelope[propertyName] == null) {
            throw new IllegalArgumentException("The envelope's $propertyName must not be null")
        }
    }

}
