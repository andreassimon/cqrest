package de.oneos.eventsourcing

class AssertEventEnvelope {

    static isValid(EventEnvelope eventEnvelope) {
        AssertEventEnvelope.notEmpty(eventEnvelope, 'applicationName')
        AssertEventEnvelope.notEmpty(eventEnvelope, 'boundedContextName')
        AssertEventEnvelope.notEmpty(eventEnvelope, 'aggregateName')
        AssertEventEnvelope.notNull(eventEnvelope, 'aggregateId')
        AssertEventEnvelope.notNull(eventEnvelope, 'sequenceNumber')
        AssertEventEnvelope.notEmpty(eventEnvelope, 'eventName')
        AssertEventEnvelope.notNull(eventEnvelope, 'timestamp')
    }

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
