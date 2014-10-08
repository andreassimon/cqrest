package org.cqrest.eventstore

import org.cqrest.eventsourcing.*


interface EventStore extends EventSupplier {

    public static final UUID NO_CORRELATION_ID = null
    public static final String USER_UNKNOWN = null

    public Correlation inUnitOfWork(String application, String boundedContext, UUID correlationId, String user, Closure closure)

    UnitOfWork createUnitOfWork(String application, String boundedContext, UUID correlationId, String user)

    void commit(UnitOfWork unitOfWork) throws IllegalArgumentException, EventCollisionOccurred

    List<EventEnvelope> findAll(Map<String, ?> criteria)

}
