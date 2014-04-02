package de.oneos.eventstore.inmemory

import de.oneos.eventsourcing.EventEnvelope


class AnEventEnvelope {
    public static final String APPLICATION_NAME = 'APPLICATION'
    public static final String BOUNDED_CONTEXT_NAME = 'BOUNDED CONTEXT'
    public static UUID ORDER_ID = UUID.fromString('836ed0d1-e87f-4d70-80f3-7aa44d00ed5d')
    public static UUID ANOTHER_ORDER_ID = UUID.fromString('92bc8efe-0f5e-42f7-8dd6-3029d2d1a4eb')
    public static UUID CUSTOMER_ID = UUID.fromString('3faeb1ad-6d46-458e-9b77-ee3a6e0ff3ce')
    public static UUID NO_CORRELATION_ID = null
    public static String USER_UNKNOWN = null

    private String aggregateName = 'Order'
    private String eventName = 'Order line was added'
    private UUID aggregateId = ANOTHER_ORDER_ID


    static AnEventEnvelope anEventEnvelope() {
        new AnEventEnvelope()
    }

    AnEventEnvelope withEventName(String eventName) {
        this.eventName = eventName
        return this
    }

    AnEventEnvelope withAggregateName(String aggregateName) {
        this.aggregateName = aggregateName
        return this
    }

    AnEventEnvelope withAggregateId(UUID aggregateId) {
        this.aggregateId = aggregateId
        return this
    }

    EventEnvelope build() {
        new EventEnvelope(APPLICATION_NAME, BOUNDED_CONTEXT_NAME, aggregateName, aggregateId, [eventName: eventName, eventAttributes: [:]], NO_CORRELATION_ID, USER_UNKNOWN)
    }

}
