package de.oneos.eventstore


interface EventStore {

    public static final UUID NO_CORRELATION_ID = null
    public static final String USER_UNKNOWN = null

    void setPublishers(List<EventPublisher> eventPublishers)

    void inUnitOfWork(UUID correlationId, String user, Closure closure)

    UnitOfWork createUnitOfWork(UUID correlationId, String user)

    void commit(UnitOfWork unitOfWork) throws IllegalArgumentException, EventCollisionOccurred

    List<EventEnvelope> loadEventEnvelopes(UUID aggregateId)

}
