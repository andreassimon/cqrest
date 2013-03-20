package de.oneos.eventsourcing


interface AggregateFactory {

    public <A> A newInstance(Class<A> rawAggregateClass, UUID aggregateId, List<Event> aggregateHistory)

}
