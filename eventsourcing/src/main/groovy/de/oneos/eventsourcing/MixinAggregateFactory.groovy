package de.oneos.eventsourcing

import static java.lang.System.identityHashCode


class MixinAggregateFactory implements AggregateFactory {

    Map<Integer, UUID> aggregateIds = Collections.synchronizedMap([:])
    Map<Integer, EventAggregator> eventAggregators = Collections.synchronizedMap([:])

    public <A> A newInstance(Class<A> rawAggregateClass, UUID aggregateId, EventAggregator eventAggregator, List<Event> aggregateHistory) {
        def instance = rawAggregateClass.newInstance()
        instance.metaClass = defineExpandoMetaClass(rawAggregateClass) {
            emit = { Event event ->
                eventAggregators[identityHashCode(delegate)].publishEvent(
                    // TODO Validate that rawAggregateClass has these coordinates -> fail early
                    rawAggregateClass.applicationName,
                    rawAggregateClass.boundedContextName,
                    rawAggregateClass.aggregateName,
                    aggregateIds[identityHashCode(delegate)],
                    event
                )
                event.applyTo(delegate)
            }
        }
        aggregateIds[identityHashCode(instance)] = aggregateId
        eventAggregators[identityHashCode(instance)] = eventAggregator
        aggregateHistory.each { event ->
            event.applyTo(instance)
        }
        return instance
    }

    static defineExpandoMetaClass(Class theClass, Closure definition) {
        ExpandoMetaClass expandoAggregateClass = new ExpandoMetaClass(theClass)
        expandoAggregateClass.define(definition).initialize()
        expandoAggregateClass
    }

}
