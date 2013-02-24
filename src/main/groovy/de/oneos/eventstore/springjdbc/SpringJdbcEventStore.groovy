package de.oneos.eventstore.springjdbc

import de.oneos.eventstore.EventStore
import de.oneos.eventstore.UnitOfWork

class SpringJdbcEventStore implements EventStore {

    @Override
    UnitOfWork createUnitOfWork() {
        return new UnitOfWork()
    }

}
