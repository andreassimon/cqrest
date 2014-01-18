package de.oneos.eventsourcing

import org.apache.commons.logging.*


class DefaultAggregateFactory implements AggregateFactory {
    Log log = LogFactory.getLog(DefaultAggregateFactory)

    public <A> A newInstance(Class<A> aggregateClass, UUID aggregateId, List aggregateHistory) {
        assertAggregateNameIsDefined(aggregateClass)

        def instance = aggregateClass.newInstance(aggregateId)
        aggregateHistory.each { event ->
            try {
                instance.invokeMethod(event['eventName'] as String, event['eventAttributes'])
            } catch(MissingMethodException e) {
                throw new EventNotApplicable(event, instance, e)
            }
        }
        return instance
    }

    public static void assertAggregateNameIsDefined(Class rawAggregateClass) {
        if (!hasMethod(rawAggregateClass, 'getAggregateName')) {
            throw new MissingAggregateName(rawAggregateClass)
        }
    }

    public static boolean hasMethod(Class rawAggregateClass, String methodName) {
        try {
            return rawAggregateClass.getDeclaredMethod(methodName) ||
            rawAggregateClass.metaClass.methods.find { methodName == it.name }
        } catch(NoSuchMethodException e) {
            return false
        }
    }

}
