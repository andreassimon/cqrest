package de.oneos.eventstore

import static java.lang.Math.*

import de.oneos.eventsourcing.*


class UnitOfWork implements EventAggregator {

    protected EventStore eventStore
    protected List<EventEnvelope> publishedEventEnvelopes = []

    Map<Integer, UUID> aggregateIds = Collections.synchronizedMap([:])
    Map<Class, ExpandoMetaClass> expandoAggregateClasses = Collections.synchronizedMap([:])

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

    def get(Class aggregateClass, UUID aggregateId, Closure eventFactory) {
        def aggregate = newAggregateInstance(aggregateClass, aggregateId)

        List<EventEnvelope> eventEnvelopes = loadEventEnvelopes(aggregateClass, aggregateId, eventFactory)

        updateSequenceNumbers(aggregateClass, aggregateId, eventEnvelopes)
//        TODO Apply the loaded events to the aggregate
//        eventEnvelopes.each { envelope ->
//            envelope.applyEventTo(aggregate)
//        }
        return aggregate
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

    protected newAggregateInstance(Class aggregateClass, UUID aggregateId) {
        def aggregate = aggregateClass.newInstance()
        aggregate.metaClass = expando(aggregateClass, this)
        aggregate.aggregateId = aggregateId
        aggregate
    }

    protected expando(Class aggregateClass, UnitOfWork unitOfWork) {
        if (!expandoAggregateClasses.containsKey(aggregateClass)) {
            expandoAggregateClasses[aggregateClass] = buildExpandoAggregateClass(aggregateClass, unitOfWork)
        }
        expandoAggregateClasses[aggregateClass]
    }

    protected buildExpandoAggregateClass(Class aggregateClass, UnitOfWork unitOfWork) {
        defineExpandoMetaClass(aggregateClass) {
            setAggregateId = { thisAggregateId ->
                aggregateIds[System.identityHashCode(delegate)] = thisAggregateId
            }

            getAggregateId = {->
                aggregateIds[System.identityHashCode(delegate)]
            }

            publishEvent = { event ->
                unitOfWork.publishEvent(
                    aggregateClass.applicationName,
                    aggregateClass.boundedContextName,
                    aggregateClass.aggregateName,
                    delegate.aggregateId,
                    event
                )
                // TODO Immediately apply the event to the aggregate
            }
        }
    }

    static defineExpandoMetaClass(Class theClass, Closure definition) {
        ExpandoMetaClass expandoAggregateClass = new ExpandoMetaClass(theClass)
        expandoAggregateClass.define(definition).initialize()
        expandoAggregateClass
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
