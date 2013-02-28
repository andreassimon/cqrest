package de.oneos.eventsourcing


interface AggregateFactory {

    public <A> A newInstance(Map aggregateProperties, Class<A> rawAggregateClass)

}
