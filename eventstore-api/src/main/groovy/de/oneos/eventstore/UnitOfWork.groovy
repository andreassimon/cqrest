package de.oneos.eventstore

import static java.lang.Math.*
import static java.util.Collections.*

import de.oneos.eventsourcing.*


class UnitOfWork {

    protected final EventStore eventStore
    protected final String applicationName
    protected final String boundedContextName

    protected AggregateFactory aggregateFactory
    protected Collection attachedAggregates = new LinkedList()
    protected Map<Integer, Integer> aggregateVersion = synchronizedMap([:])


    UnitOfWork(EventStore eventStore, String application, String boundedContext) {
        assert eventStore != null
        assert application != null; assert !application.isEmpty()
        assert boundedContext != null; assert !boundedContext.isEmpty()

        this.eventStore = eventStore
        this.applicationName = application
        this.boundedContextName = boundedContext
        this.aggregateFactory = new DefaultAggregateFactory()
    }


    public <A> A get(Class<A> aggregateClass, UUID aggregateId, Closure eventFactory) {
        List<EventEnvelope> eventEnvelopes = loadEventEnvelopes(aggregateClass, aggregateId, eventFactory)

        def aggregate = newAggregateInstance(aggregateClass, aggregateId, eventEnvelopes)
        attach(aggregate)
        updateVersion(aggregate, eventEnvelopes)

        return aggregate
    }

    protected newAggregateInstance(Class aggregateClass, UUID aggregateId, List<EventEnvelope> eventEnvelopes) {
        aggregateFactory.newInstance(aggregateClass, aggregateId, eventEnvelopes.collect { it.event })
    }

    protected updateVersion(aggregate, Collection<EventEnvelope> eventEnvelopes) {
        setVersion(aggregate, maximumSequenceNumber(eventEnvelopes))
    }

    protected setVersion(aggregate, calculatedVersion) {
        aggregateVersion[System.identityHashCode(aggregate)] = calculatedVersion
    }

    protected static maximumSequenceNumber(Collection<EventEnvelope> eventEnvelopes) {
        eventEnvelopes.inject(-1) { int maximumSequenceNumber, envelope -> max(maximumSequenceNumber, envelope.sequenceNumber) }
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

    void attach(Object[] aggregates) {
        aggregates.each { attach(it) }
    }

    void attach(aggregate) {
        attachedAggregates << aggregate
    }

    void eachEventEnvelope(Closure callback) {
        attachedAggregates.collect { aggregate ->
            (0..(aggregate.newEvents.size()-1)).collect { int i ->
                new EventEnvelope(
                    applicationName,
                    boundedContextName,
                    aggregate.aggregateName,
                    aggregate.id,
                    aggregate.newEvents[i],
                    version(aggregate) + 1 + i
                )
            }
        }.flatten().each { callback(it) }
    }

    protected version(aggregate) {
        def aggregateHash = System.identityHashCode(aggregate)
        if(aggregateVersion.containsKey(aggregateHash)) {
            return aggregateVersion[aggregateHash]
        }
        return -1
    }

    void flush() {
        attachedAggregates.each { it.flushEvents() }
    }

}
