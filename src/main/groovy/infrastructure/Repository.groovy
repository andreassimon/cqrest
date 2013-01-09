package infrastructure

public interface Repository {
    def getEventsFor(Class aggregateClass, UUID aggregateId)
}
