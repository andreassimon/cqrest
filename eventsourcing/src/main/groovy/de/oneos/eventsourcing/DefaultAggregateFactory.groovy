package de.oneos.eventsourcing

import org.apache.commons.logging.*


class DefaultAggregateFactory implements AggregateFactory {
    Log log = LogFactory.getLog(DefaultAggregateFactory)

    public <A> A newInstance(Class<A> aggregateClass, UUID aggregateId, List<Event> aggregateHistory) {
        assertAggregateNameIsDefined(aggregateClass)

        def instance = aggregateClass.newInstance(aggregateId)
        aggregateHistory.each { event ->
            log.warn('Usage of interface `Event` is deprecated! <DefaultAggregateFactory.newInstance(Class, UUID, List<Event>)>')
            log.warn("         Event $event is applied to $instance!".toString())
            try {
                instance.invokeMethod(event.eventName, event.serializableForm)
            } catch(MissingMethodException e) {
                throw new EventNotApplicable(event, instance, e)
            }
        }
        return instance
    }

    protected static assertAggregateNameIsDefined(Class rawAggregateClass) {
        if (!hasMethod(rawAggregateClass, 'getAggregateName')) {
            throw new MissingAggregateName(rawAggregateClass)
        }
    }

    protected static hasMethod(Class rawAggregateClass, String methodName) {
        rawAggregateClass.metaClass.methods.find { methodName == it.name }
    }

}
