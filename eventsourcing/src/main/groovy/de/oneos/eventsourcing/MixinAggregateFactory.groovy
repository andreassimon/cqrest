package de.oneos.eventsourcing

import static java.lang.System.identityHashCode


class MixinAggregateFactory implements AggregateFactory {

    Map<Integer, UUID> aggregateIds = Collections.synchronizedMap([:])
    Map<Integer, EventAggregator> eventAggregators = Collections.synchronizedMap([:])

    public <A> A newInstance(Class<A> rawAggregateClass, UUID aggregateId, EventAggregator eventAggregator, List<Event> aggregateHistory) {
        assertApplicationNameIsDefined(rawAggregateClass)
        assertBoundedContextNameIsDefined(rawAggregateClass)
        assertAggregateNameIsDefined(rawAggregateClass)

        def instance = rawAggregateClass.newInstance()
        instance.metaClass = defineExpandoMetaClass(rawAggregateClass) {
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
        aggregateIds[identityHashCode(instance)] = aggregateId
        eventAggregators[identityHashCode(instance)] = eventAggregator
        aggregateHistory.each { event ->
            assertIsApplicableTo(event, instance)
            event.applyTo(instance)
        }
        return instance
    }

    protected static assertIsApplicableTo(Event event, instance) {
        if (!isApplicableTo(event, instance)) {
            throw new EventNotApplicable(event, instance)
        }
    }

    protected static assertAggregateNameIsDefined(Class rawAggregateClass) {
        if (!hasMethod(rawAggregateClass, 'getAggregateName')) {
            throw new MissingAggregateName(rawAggregateClass)
        }
    }

    protected static assertBoundedContextNameIsDefined(Class rawAggregateClass) {
        if (!hasMethod(rawAggregateClass, 'getBoundedContextName')) {
            throw new MissingBoundedContextName(rawAggregateClass)
        }
    }

    protected static assertApplicationNameIsDefined(Class rawAggregateClass) {
        if (!hasMethod(rawAggregateClass, 'getApplicationName')) {
            throw new MissingApplicationName(rawAggregateClass)
        }
    }

    protected static hasMethod(Class rawAggregateClass, String methodName) {
        rawAggregateClass.metaClass.methods.find { methodName == it.name }
    }

    protected static isApplicableTo(Event event, instance) {
        event.metaClass.methods.find { MetaMethod it ->
            !it.abstract &&
            it.name == 'applyTo' &&
            it.parameterTypes.length == 1 &&
            it.parameterTypes[0].theClass.isAssignableFrom(instance.class)
        }
    }

    static defineExpandoMetaClass(Class theClass, Closure definition) {
        ExpandoMetaClass expandoAggregateClass = new ExpandoMetaClass(theClass)
        expandoAggregateClass.define(definition).initialize()
        expandoAggregateClass
    }

}
