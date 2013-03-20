package de.oneos.eventstore

import static java.lang.Math.*

import de.oneos.eventsourcing.*


class UnitOfWork {

    protected final EventStore eventStore
    protected final String applicationName
    protected final String boundedContextName

    protected AggregateFactory aggregateFactory
    protected List<EventEnvelope> publishedEventEnvelopes = []

    Map<String, Map<String, Map<String, Map<UUID, Integer>>>> nextSequenceNumbers =
        emptyMapWithDefault(
            SequenceNumberGenerator.newInstance)()


    UnitOfWork(EventStore eventStore, String application, String boundedContext) {
        assert eventStore != null
        assert application != null; assert !application.isEmpty()
        assert boundedContext != null; assert !boundedContext.isEmpty()

        this.eventStore = eventStore
        this.applicationName = application
        this.boundedContextName = boundedContext
        this.aggregateFactory = new DefaultAggregateFactory()
    }


    static Closure<Map> emptyMapWithDefault(Closure defaultFactory) {
        return {
            MapWithDefault.newInstance([:], defaultFactory)
        }
    }

    def get(Class aggregateClass, UUID aggregateId, Closure eventFactory) {
        List<EventEnvelope> eventEnvelopes = loadEventEnvelopes(aggregateClass, aggregateId, eventFactory)

        def aggregate = newAggregateInstance(aggregateClass, aggregateId, eventEnvelopes)
        updateSequenceNumbers(aggregateClass, aggregateId, eventEnvelopes)

        return aggregate
    }

    protected newAggregateInstance(Class aggregateClass, UUID aggregateId, List<EventEnvelope> eventEnvelopes) {
        aggregateFactory.newInstance(aggregateClass, aggregateId, eventEnvelopes.collect { it.event })
    }

    protected updateSequenceNumbers(Class aggregateClass, UUID aggregateId, List<EventEnvelope> eventEnvelopes) {
        nextSequenceNumbers(aggregateClass)[aggregateId] = maximumSequenceNumber(eventEnvelopes) + 1
    }

    protected maximumSequenceNumber(List<EventEnvelope> eventEnvelopes) {
        eventEnvelopes.inject(0) { int maximumSequenceNumber, envelope -> max(maximumSequenceNumber, envelope.sequenceNumber) }
    }

    protected nextSequenceNumbers(Class aggregateClass) {
        nextSequenceNumbers[aggregateClass.aggregateName]
    }

    protected loadEventEnvelopes(Class aggregateClass, UUID aggregateId, Closure eventFactory) {
        eventStore.loadEventEnvelopes(
            this.applicationName,
            this.boundedContextName,
            aggregateClass.aggregateName,
            aggregateId,
            eventFactory
        )
    }

    void publishEvent(String aggregateName, UUID aggregateId, Event event) {
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
        nextSequenceNumbers[aggregateName][aggregateId]
    }

    void eachEventEnvelope(Closure callback) {
        publishedEventEnvelopes.each { callback(it) }
    }

}
