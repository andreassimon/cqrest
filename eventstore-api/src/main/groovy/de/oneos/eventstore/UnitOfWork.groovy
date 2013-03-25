package de.oneos.eventstore

import static java.util.Collections.*

import de.oneos.eventsourcing.*
import de.oneos.validation.*


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


    public <A> A get(Class<A> aggregateClass, UUID aggregateId) {
        List<EventEnvelope> eventEnvelopes = loadEventEnvelopes(aggregateId)

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
        eventEnvelopes.inject(-1) { int maximumSequenceNumber, envelope -> Math.max(maximumSequenceNumber, envelope.sequenceNumber) }
    }

    protected loadEventEnvelopes(UUID aggregateId) {
        eventStore.loadEventEnvelopes(aggregateId)
    }

    void attach(Object[] aggregates) {
        aggregates.each { attach(it) }
    }

    // allows fluent code, e.g.
    //   def newAggregate = attach(new Aggregate(...))
    def attach(aggregate) {
        attachedAggregates << aggregate
        aggregate
    }

    void eachEventEnvelope(Closure callback) throws ValidationException {
        attachedAggregates.
            findAll { Validatable.isAssignableFrom(it.getClass()) }.
            findAll { Validatable it -> !it.isValid() }.
            each { Validatable it -> throw new ValidationException(it, it.validationMessage()) }

        attachedAggregates.collect { aggregate ->
            aggregate.newEvents.inject([]) { List eventEnvelopes, Event newEvent ->
                eventEnvelopes + new EventEnvelope(
                    applicationName,
                    boundedContextName,
                    aggregate.aggregateName,
                    aggregate.id,
                    newEvent,
                    version(aggregate) + 1 + eventEnvelopes.size()
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
