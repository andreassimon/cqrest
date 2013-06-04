package de.oneos.eventsourcing


interface AggregateFactory {

    @Deprecated
    public <A> A newInstance(Class<A> rawAggregateClass, UUID aggregateId, List aggregateHistory)

}
