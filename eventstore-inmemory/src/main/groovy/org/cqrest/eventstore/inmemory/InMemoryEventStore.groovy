package org.cqrest.eventstore.inmemory

import org.apache.commons.logging.*

import org.cqrest.eventsourcing.*
import org.cqrest.eventstore.*


class InMemoryEventStore implements EventStore, EventSupplier, EventStream {
    public static Log log = LogFactory.getLog(InMemoryEventStore)


    List<EventEnvelope> history = []
    Subscribers subscribers = new Subscribers(log)
    EventBus eventBus = new StubEventBus()


    @Override
    @Deprecated
    void subscribeTo(Map<String, ?> criteria, EventConsumer eventConsumer) {
        assert null != eventConsumer
        subscribers.observable.subscribe(new EventConsumerAdapter(eventConsumer))
    }

    @Override
    public Correlation inUnitOfWork(String application, String boundedContext, UUID correlationId, String user, Closure closure) {
        Correlation correlation = new Correlation(correlationId)
        eventBus.subscribeCorrelation(correlation)
        def unitOfWork = createUnitOfWork(application, boundedContext, correlationId, user)
        unitOfWork.with(closure)
        commit(unitOfWork)
        return correlation
    }

    @Override
    UnitOfWork createUnitOfWork(String application, String boundedContext, UUID correlationId, String user) {
        new UnitOfWork(this, application, boundedContext, correlationId, user)
    }

    @Override
    void commit(UnitOfWork unitOfWork) throws IllegalArgumentException, EventCollisionOccurred {
        saveEnvelopes(unitOfWork.eventEnvelopes)
        unitOfWork.flush()
    }

    public void saveEnvelopes(List<EventEnvelope> envelopes) {
        envelopes.each validateEnvelope
        envelopes.each saveEnvelope
    }

    protected Closure validateEnvelope = { EventEnvelope it ->
        AssertEventEnvelope.isValid(it)
        assertIsUnique(it)
    }

    protected Closure saveEnvelope = { eventEnvelope ->
        history << eventEnvelope
        subscribers.publish(eventEnvelope)
    }

    void addEventEnvelope(UUID aggregateId, String application, String boundedContext, final String aggregateType, Event event, int sequenceNumber) {
        EventEnvelope newEnvelope = new EventEnvelope(
            application,
            boundedContext,
            aggregateType,
            aggregateId, event, sequenceNumber,
            NO_CORRELATION_ID,
            USER_UNKNOWN
        )
        validateEnvelope.call(newEnvelope)
        saveEnvelope.call(newEnvelope)
    }

    protected assertIsUnique(EventEnvelope eventEnvelope) {
        if (history.find { persistedEnvelope ->
            persistedEnvelope.aggregateId == eventEnvelope.aggregateId &&
                persistedEnvelope.sequenceNumber == eventEnvelope.sequenceNumber
        }) {
            throw new EventCollisionOccurred(eventEnvelope)
        }
    }

    @Override
    List<EventEnvelope> findAll(Map<String, ?> criteria) {
        return history.findAll(new CriteriaFilter(criteria).test)
    }

    @Override
    String toString() { "InMemoryEventStore" }

    @Override
    @Deprecated
    void withEventEnvelopes(Map<String, ?> criteria, Closure block) {
        findAll(criteria).each(block)
    }

    @Override
    org.cqrest.reactive.Observable<EventEnvelope> observe(Map<String, ?> criteria = [:]) {
        return new org.cqrest.reactive.Observable<EventEnvelope>(
            rx.Observable.concat(
              rx.Observable.from(findAll(criteria)),
              subscribers.observable.filter(new CriteriaFilter(criteria))
            )
        )
    }

}
