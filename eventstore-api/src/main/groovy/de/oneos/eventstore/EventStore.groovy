package de.oneos.eventstore

import de.oneos.eventsourcing.*


interface EventStore extends EventSupplier {

    public static final UUID NO_CORRELATION_ID = null
    public static final String USER_UNKNOWN = null

    void setEventConsumers(List<EventConsumer> eventConsumers)

    public Correlation inUnitOfWork(String application, String boundedContext, UUID correlationId, String user, Closure closure)

    UnitOfWork createUnitOfWork(String application, String boundedContext, UUID correlationId, String user)

    void commit(UnitOfWork unitOfWork) throws IllegalArgumentException, EventCollisionOccurred

    List<EventEnvelope> findAll(Map<String, ?> criteria)

}
