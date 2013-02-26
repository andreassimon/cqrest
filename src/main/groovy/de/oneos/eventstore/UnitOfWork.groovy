package de.oneos.eventstore

import de.oneos.eventsourcing.*


class UnitOfWork {

    protected EventStore eventStore
    protected List<EventEnvelope> publishedEventEnvelopes = []

    Map<String, Map<String, Map<String, Map<UUID, Integer>>>> nextSequenceNumbers =
        emptyMapWithDefault(
            emptyMapWithDefault(
                emptyMapWithDefault(
                    SequenceNumberGenerator.newInstance)))()


    UnitOfWork(EventStore eventStore) {
        this.eventStore = eventStore
    }


    static Closure<Map> emptyMapWithDefault(Closure defaultFactory) {
        return {
            MapWithDefault.newInstance([:], defaultFactory)
        }
    }

    def get(Class aggregateClass, String applicationName, String boundedContextName, String aggregateName, UUID aggregateId, Closure eventFactory) {
        eventStore.buildAggregate(aggregateClass, applicationName, boundedContextName, aggregateName, aggregateId, eventFactory)
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
