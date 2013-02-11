package domain

import infrastructure.persistence.DefaultEventStore
import domain.aggregates.Device
import infrastructure.persistence.UnknownAggregate
import infrastructure.persistence.AggregateAlreadyExists

class Repository {
    DefaultEventStore eventStore
    String applicationName
    String boundedContextName

    Repository(DefaultEventStore eventStore, String applicationName, String boundedContextName) {
        this.eventStore = eventStore
        this.applicationName = applicationName
        this.boundedContextName = boundedContextName
    }

    void assertDeviceDoesNotExist(UUID deviceId) {
        try {
            if (getDevice(deviceId)) {
                throw new AggregateAlreadyExists(Device, deviceId)
            }
        } catch (UnknownAggregate e) {
            // This is expected
        }
    }

    def getDevice(UUID deviceId) throws UnknownAggregate {
        eventStore.getAggregate(applicationName, boundedContextName, 'Device', Device, deviceId)
    }
}
