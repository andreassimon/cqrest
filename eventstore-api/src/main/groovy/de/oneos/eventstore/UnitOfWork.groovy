package de.oneos.eventstore

import de.oneos.eventsourcing.*
import de.oneos.validation.*


class UnitOfWork {

    protected final EventStore eventStore
    protected final String applicationName
    protected final String boundedContextName
    protected final UUID correlationId
    protected final String user

    protected AggregateFactory aggregateFactory
    protected Collection attachedAggregates = new LinkedList()


    UnitOfWork(EventStore eventStore, String application, String boundedContext, UUID correlationId, String user) {
        assert eventStore != null
        assert application != null; assert !application.isEmpty()
        assert boundedContext != null; assert !boundedContext.isEmpty()

        this.eventStore = eventStore
        this.applicationName = application
        this.boundedContextName = boundedContext
        this.correlationId      = correlationId
        this.user               = user
        this.aggregateFactory = new DefaultAggregateFactory()
    }


    public <A> A get(Class<A> aggregateClass, UUID aggregateId) {
        List<EventEnvelope> eventEnvelopes = loadEventEnvelopes(aggregateId)

        if(eventEnvelopes.empty) {
            throw new AggregateNotFoundException(aggregateClass, aggregateId)
        }

        A aggregate = newAggregateInstance(aggregateClass, aggregateId, eventEnvelopes.collect { it.event })
        attach(aggregate)
        aggregate.setVersion(maximumSequenceNumber(eventEnvelopes))

        return aggregate
    }

    protected <A> A newAggregateInstance(Class<A> aggregateClass, UUID aggregateId, List events) {
        aggregateFactory.newInstance(aggregateClass, aggregateId, events)
    }

    protected static maximumSequenceNumber(Collection<EventEnvelope> eventEnvelopes) {
        eventEnvelopes.max { it.sequenceNumber }.sequenceNumber
    }

    protected loadEventEnvelopes(UUID aggregateId) {
        eventStore.findAll(aggregateId: aggregateId)
    }

    void attach(Object[] aggregates) {
        aggregates.each { attach(it) }
    }

    /**
     * @return the parameter, allows fluent code, e.g. <code>attach(new Aggregate(...)).doSomething()</code>
     */
    public <A> A attach(A aggregate) {
        assert aggregate.aggregateName != null

        attachedAggregates << aggregate
        aggregate
    }

    void eachEventEnvelope(Closure callback) throws ValidationException {
        attachedAggregates.
            findAll { !((Collection)it.getNewEvents()).empty }.
            findAll { Validatable.isAssignableFrom(it.getClass()) }.
            findAll { Validatable it -> !it.isValid() }.
            each { Validatable it -> throw new ValidationException(it, it.validationMessage()) }

        attachedAggregates.collect { aggregate ->
            aggregate.getNewEvents().inject([]) { List eventEnvelopes, Event newEvent ->
                eventEnvelopes + new EventEnvelope(
                    this.applicationName,
                    this.boundedContextName,
                    aggregate.aggregateName,
                    aggregate.id,
                    newEvent,
                    aggregate.getVersion() + 1 + eventEnvelopes.size(),
                    this.correlationId,
                    this.user
                )
            }
        }.flatten().each { callback(it) }
    }

    void flush() {
        attachedAggregates.each {
            try {
                it.flushEvents()
            } catch (MissingMethodException e) { /* Ignore */ }
        }
    }

}
