package oneos.test.domain

import infrastructure.persistence.BoundedContextRepository
import de.oneos.cqrs.eventstore.EventStore
import de.oneos.cqrs.eventstore.UnknownAggregate

class DeviceRepository extends BoundedContextRepository {

    DeviceRepository(EventStore eventStore) {
        super(eventStore, 'CQRS Core Library', 'Test')
    }

    void assertDeviceDoesNotExist(UUID deviceId) {
        assertAggregateDoesNotExist('Device', oneos.test.domain.aggregates.Device, deviceId)
    }

    def getDevice(UUID deviceId) throws UnknownAggregate {
        getAggregate('Device', oneos.test.domain.aggregates.Device, deviceId)
    }
}
