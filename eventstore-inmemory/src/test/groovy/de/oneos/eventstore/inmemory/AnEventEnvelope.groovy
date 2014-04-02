package de.oneos.eventstore.inmemory

import de.oneos.eventsourcing.EventEnvelope
import de.oneos.eventstore.EventStore_ContractTest


class AnEventEnvelope {
    public static final String APPLICATION_NAME = 'APPLICATION'
    public static final String BOUNDED_CONTEXT_NAME = 'BOUNDED CONTEXT'
    public static UUID ORDER_ID = UUID.fromString('836ed0d1-e87f-4d70-80f3-7aa44d00ed5d')
    public static UUID ANOTHER_ORDER_ID = UUID.fromString('92bc8efe-0f5e-42f7-8dd6-3029d2d1a4eb')
    public static UUID CUSTOMER_ID = UUID.fromString('3faeb1ad-6d46-458e-9b77-ee3a6e0ff3ce')
    public static UUID NO_CORRELATION_ID = null
    public static String USER_UNKNOWN = null

    static EventEnvelope build() {
        new EventEnvelope(APPLICATION_NAME, BOUNDED_CONTEXT_NAME, EventStore_ContractTest.Order.aggregateName, ANOTHER_ORDER_ID, EventStore_ContractTest.orderLineWasRemoved(), NO_CORRELATION_ID, USER_UNKNOWN)
    }

}
