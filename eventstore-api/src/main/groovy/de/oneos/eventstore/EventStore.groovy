package de.oneos.eventstore


interface EventStore extends EventSupplier {

    public static final UUID NO_CORRELATION_ID = null
    public static final String USER_UNKNOWN = null

    void setEventProcessors(List<EventProcessor> eventProcessors)

    // TODO Replace with subscribeTo(...)
    void addEventProcessor(EventProcessor eventProcessor)

    public <T> T inUnitOfWork(String application, String boundedContext, UUID correlationId, String user, Closure<T> closure)

    UnitOfWork createUnitOfWork(String application, String boundedContext, UUID correlationId, String user)

    void commit(UnitOfWork unitOfWork) throws IllegalArgumentException, EventCollisionOccurred

    List<EventEnvelope> findAll(Map<String, ?> criteria)

}
