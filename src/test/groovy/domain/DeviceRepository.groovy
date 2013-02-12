package domain

import infrastructure.persistence.BoundedContextRepository
import infrastructure.persistence.EventStore
import infrastructure.persistence.UnknownAggregate

class DeviceRepository extends BoundedContextRepository {

    DeviceRepository(EventStore eventStore) {
        super(eventStore, 'CQRS Core Library', 'Test')
    }

    void assertDeviceDoesNotExist(UUID deviceId) {
        assertAggregateDoesNotExist('Device', domain.aggregates.Device, deviceId)
    }

    def getDevice(UUID deviceId) throws UnknownAggregate {
        getAggregate('Device', domain.aggregates.Device, deviceId)
    }
}
