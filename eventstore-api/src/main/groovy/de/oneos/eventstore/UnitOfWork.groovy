package de.oneos.eventstore

import static java.util.Collections.*

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


    public List<EventEnvelope> findAll(Map<String, ?> criteria) {
        return eventStore.findAll(criteria)
    }

    public Repositories getRepositories() {
        new Repositories(unitOfWork: this)
    }

    public <A> A get(Class<A> aggregateClass, UUID aggregateId) {
        List<EventEnvelope> eventEnvelopes = loadEventEnvelopes(aggregateId)

        def aggregate = newAggregateInstance(aggregateClass, aggregateId, eventEnvelopes)
        attach(aggregate)
        aggregate.setVersion(maximumSequenceNumber(eventEnvelopes))

        return aggregate
    }

    protected newAggregateInstance(Class aggregateClass, UUID aggregateId, List<EventEnvelope> eventEnvelopes) {
        aggregateFactory.newInstance(aggregateClass, aggregateId, eventEnvelopes.collect { it.event })
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
