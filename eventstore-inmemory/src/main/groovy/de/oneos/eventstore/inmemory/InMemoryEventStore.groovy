package de.oneos.eventstore.inmemory

import org.apache.commons.logging.*

import de.oneos.eventsourcing.*
import de.oneos.eventstore.*


class InMemoryEventStore implements EventStore, EventSupplier, EventStream {
    static Log log = LogFactory.getLog(InMemoryEventStore)


    List<EventEnvelope> history = []
    Collection<org.cqrest.reactive.Observer<EventEnvelope>> observers = []
    EventBus eventBus = new StubEventBus()




    @Override
    @Deprecated
    void subscribeTo(Map<String, ?> criteria, EventConsumer eventConsumer) {
        assert null != eventConsumer
        observers.add(new EventConsumerAdapter(eventConsumer))
        try {
            eventConsumer.wasRegisteredAt(this)
        } catch(e) {
            log.warn("Exception occurred during registration of $eventConsumer at $this", e)
        }
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
        observers.each { org.cqrest.reactive.Observer observer ->
            try {
                observer.onNext(eventEnvelope)
            } catch(e) {
                log.error("${e.getClass().getCanonicalName()}: Couldn't process $eventEnvelope in $observer", e)
            }
        }
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
        return history.findAll { eventEnvelope ->
            criteria.every { attribute, value ->
                if(value instanceof Collection) {
                    return value.contains(eventEnvelope[attribute])
                }
                value == eventEnvelope[attribute]
            }
        }
    }

    @Override
    String toString() { "InMemoryEventStore" }

    @Override
    @Deprecated
    void withEventEnvelopes(Map<String, ?> criteria, Closure block) {
        findAll(criteria).each(block)
    }

    @Override
    org.cqrest.reactive.Observable<EventEnvelope> observe(Map<String, ?> criteria) {
        return new org.cqrest.reactive.Observable<EventEnvelope>(
            rx.Observable.from(findAll(criteria))
        )
    }

}
