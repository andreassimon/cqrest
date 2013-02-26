package de.oneos.eventstore

import de.oneos.eventsourcing.*


class UnitOfWork {

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

    def get(Class aggregateClass, String applicationName, String boundedContextName, String aggregateName, UUID aggregateId, Closure eventFactory) {
        def aggregate = eventStore.buildAggregate(aggregateClass, applicationName, boundedContextName, aggregateName, aggregateId, eventFactory)
        aggregate.metaClass = expando(aggregateClass, applicationName, boundedContextName, aggregateName, this)
        aggregate.aggregateId = aggregateId
        return aggregate
    }

    protected expando(Class aggregateClass, String applicationName, String boundedContextName, String aggregateName, UnitOfWork unitOfWork) {
        if (!expandoAggregateClasses.containsKey(aggregateClass)) {
            expandoAggregateClasses[aggregateClass] = buildExpandoAggregateClass(aggregateClass, applicationName, boundedContextName, aggregateName, unitOfWork)
        }
        expandoAggregateClasses[aggregateClass]
    }

    protected buildExpandoAggregateClass(Class aggregateClass, String applicationName, String boundedContextName, String aggregateName, UnitOfWork unitOfWork) {
        defineExpandoMetaClass(aggregateClass) {
            setAggregateId = { thisAggregateId ->
                aggregateIds[System.identityHashCode(delegate)] = thisAggregateId
            }

            getAggregateId = {->
                aggregateIds[System.identityHashCode(delegate)]
            }

            publishEvent = { event ->
                unitOfWork.publishEvent(applicationName, boundedContextName, aggregateName, delegate.aggregateId, event)
            }
        }
    }

    static defineExpandoMetaClass(Class theClass, Closure definition) {
        ExpandoMetaClass expandoAggregateClass = new ExpandoMetaClass(theClass)
        expandoAggregateClass.define(definition).initialize()
        expandoAggregateClass
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
