package de.oneos.eventsourcing

import de.oneos.eventstore.EventAggregator

import static java.lang.System.identityHashCode

class AggregateFactory {
    Map<Integer, EventAggregator> eventAggregators = Collections.synchronizedMap([:])
    Map<Integer, UUID> aggregateIds = Collections.synchronizedMap([:])

    public <A> A newInstance(Map aggregateProperties, Class<A> rawAggregateClass) {
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
                event.applyTo(delegate)
            }
        }
        aggregateProperties.each { name, value ->
            instance[name] = value
        }
        return instance
    }

    static defineExpandoMetaClass(Class theClass, Closure definition) {
        ExpandoMetaClass expandoAggregateClass = new ExpandoMetaClass(theClass)
        expandoAggregateClass.define(definition).initialize()
        expandoAggregateClass
    }

}
