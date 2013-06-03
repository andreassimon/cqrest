package de.oneos.eventsourcing

import org.apache.commons.logging.*


class DefaultAggregateFactory implements AggregateFactory {
    Log log = LogFactory.getLog(DefaultAggregateFactory)

    public <A> A newInstance(Class<A> aggregateClass, UUID aggregateId, List<Event> aggregateHistory) {
        assertAggregateNameIsDefined(aggregateClass)

        def instance = aggregateClass.newInstance(aggregateId)
        aggregateHistory.each { event ->
            assertIsApplicableTo(event, instance)
            log.warn('Usage of interface `Event` is deprecated! <DefaultAggregateFactory.newInstance(Class, UUID, List<Event>)>')
            log.warn("         Event $event is applied to $instance!".toString())
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

}
