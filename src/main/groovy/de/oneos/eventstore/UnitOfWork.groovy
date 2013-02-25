package de.oneos.eventstore

import de.oneos.eventsourcing.*


class UnitOfWork {

    List<EventEnvelope> publishedEventEnvelopes = []

    Map<String, Map<String, Map<String, Map<UUID, Integer>>>> nextSequenceNumbers =
        emptyMapWithDefault(
            emptyMapWithDefault(
                emptyMapWithDefault(
                    SequenceNumberGenerator.newInstance)))()

    static Closure<Map> emptyMapWithDefault(Closure defaultFactory) {
        return {
            MapWithDefault.newInstance([:], defaultFactory)
        }
    }

    void publishEvent(String applicationName, String boundedContextName, String aggregateName, UUID aggregateId, Event event) {
        publishedEventEnvelopes << new EventEnvelope(
            applicationName,
            boundedContextName,
            aggregateName,
            aggregateId,
            event,
            nextSequenceNumber(applicationName, boundedContextName, aggregateName, aggregateId)
        )
    }

    protected int nextSequenceNumber(applicationName, boundedContextName, aggregateName, aggregateId) {
        nextSequenceNumbers[applicationName][boundedContextName][aggregateName][aggregateId]
    }

    def eachEventEnvelope(Closure callback) {
        publishedEventEnvelopes.each { callback(it) }
    }

}
