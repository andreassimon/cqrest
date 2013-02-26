package de.oneos.eventsourcing

import de.oneos.eventstore.EventAggregator

import static java.lang.System.identityHashCode

class AggregateFactory {
    Map<Integer, EventAggregator> eventAggregators = Collections.synchronizedMap([:])
    Map<Integer, UUID> aggregateIds = Collections.synchronizedMap([:])
//    Map<Class, ExpandoMetaClass> expandoAggregateClasses = Collections.synchronizedMap([:])

    public <A> A newInstance(Class<A> rawAggregateClass, UUID aggregateId) {
        def instance = rawAggregateClass.newInstance()
        instance.metaClass = defineExpandoMetaClass(rawAggregateClass) {
            setAggregateId = { thisAggregateId ->
                aggregateIds[identityHashCode(delegate)] = thisAggregateId
            }

            setEventAggregator = { thisEventAggregator ->
                eventAggregators[identityHashCode(delegate)] = thisEventAggregator
            }

            emit = { Event event ->
                eventAggregators[identityHashCode(delegate)].publishEvent(
                    rawAggregateClass.applicationName,
                    rawAggregateClass.boundedContextName,
                    rawAggregateClass.aggregateName,
                    aggregateIds[identityHashCode(delegate)],
                    event
                )
//                TODO Immediately apply the event to the aggregate
            }
        }
        instance.aggregateId = aggregateId
        return instance
    }

//    protected expando(Class aggregateClass, UnitOfWork unitOfWork) {
//        if (!expandoAggregateClasses.containsKey(aggregateClass)) {
//            expandoAggregateClasses[aggregateClass] = buildExpandoAggregateClass(aggregateClass, unitOfWork)
//        }
//        expandoAggregateClasses[aggregateClass]
//    }
//
//    protected buildExpandoAggregateClass(Class aggregateClass, UnitOfWork unitOfWork) {
//        defineExpandoMetaClass(aggregateClass) {
//            publishEvent = { event ->
//                unitOfWork.publishEvent(
//                    aggregateClass.applicationName,
//                    aggregateClass.boundedContextName,
//                    aggregateClass.aggregateName,
//                    delegate.aggregateId,
//                    event
//                )
//            }
//        }
//    }

    static defineExpandoMetaClass(Class theClass, Closure definition) {
        ExpandoMetaClass expandoAggregateClass = new ExpandoMetaClass(theClass)
        expandoAggregateClass.define(definition).initialize()
        expandoAggregateClass
    }

}
