package de.oneos.eventstore

import static java.lang.Math.*

import de.oneos.eventsourcing.*


class UnitOfWork implements EventAggregator {

    protected EventStore eventStore
    protected AggregateFactory aggregateFactory
    protected List<EventEnvelope> publishedEventEnvelopes = []

    Map<String, Map<String, Map<String, Map<UUID, Integer>>>> nextSequenceNumbers =
        emptyMapWithDefault(
            emptyMapWithDefault(
                emptyMapWithDefault(
                    SequenceNumberGenerator.newInstance)))()


    UnitOfWork(EventStore eventStore) {
        this.eventStore = eventStore
        this.aggregateFactory = new MixinAggregateFactory()
    }


    static Closure<Map> emptyMapWithDefault(Closure defaultFactory) {
        return {
            MapWithDefault.newInstance([:], defaultFactory)
        }
    }

    def get(Class aggregateClass, UUID aggregateId, Closure eventFactory) {
        def aggregate = newAggregateInstance(aggregateClass, aggregateId)

        List<EventEnvelope> eventEnvelopes = loadEventEnvelopes(aggregateClass, aggregateId, eventFactory)

        updateSequenceNumbers(aggregateClass, aggregateId, eventEnvelopes)
        eventEnvelopes.each { envelope ->
            envelope.applyEventTo(aggregate)
        }
        return aggregate
    }

    protected newAggregateInstance(Class aggregateClass, UUID aggregateId) {
        aggregateFactory.newInstance(aggregateClass, aggregateId: aggregateId, eventAggregator: this)
    }

    protected updateSequenceNumbers(Class aggregateClass, UUID aggregateId, List<EventEnvelope> eventEnvelopes) {
        nextSequenceNumbers(aggregateClass)[aggregateId] = maximumSequenceNumber(eventEnvelopes) + 1
    }

    protected maximumSequenceNumber(List<EventEnvelope> eventEnvelopes) {
        eventEnvelopes.inject(0) { int maximumSequenceNumber, envelope -> max(maximumSequenceNumber, envelope.sequenceNumber) }
    }

    protected nextSequenceNumbers(Class aggregateClass) {
        nextSequenceNumbers[aggregateClass.applicationName][aggregateClass.boundedContextName][aggregateClass.aggregateName]
    }

    protected loadEventEnvelopes(Class aggregateClass, UUID aggregateId, Closure eventFactory) {
        eventStore.loadEventEnvelopes(
            aggregateClass.applicationName,
            aggregateClass.boundedContextName,
            aggregateClass.aggregateName,
            aggregateId,
            eventFactory
        )
    }

    @Override
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
