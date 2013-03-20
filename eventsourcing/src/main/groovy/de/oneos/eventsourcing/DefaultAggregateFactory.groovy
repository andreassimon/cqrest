package de.oneos.eventsourcing



class DefaultAggregateFactory implements AggregateFactory {

    public <A> A newInstance(Class<A> aggregateClass, UUID aggregateId, EventAggregator eventAggregator, List<Event> aggregateHistory) {
        assertApplicationNameIsDefined(aggregateClass)
        assertBoundedContextNameIsDefined(aggregateClass)
        assertAggregateNameIsDefined(aggregateClass)

        def instance = aggregateClass.newInstance(aggregateId)
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

}
