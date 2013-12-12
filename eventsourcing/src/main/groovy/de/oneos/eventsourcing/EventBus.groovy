package de.oneos.eventsourcing

import org.apache.commons.logging.*


abstract class EventBus {

    public static Log log = LogFactory.getLog(this)

    protected static EventBus INSTANCE

    static void setINSTANCE(EventBus instance) {
        INSTANCE = instance
    }


    protected static void assertEventBusIsInitialized() {
        if(null == INSTANCE) {
            log.error "EventBus is not initialized. Please set an instance of EventBus."
            throw new IllegalStateException('EventBus is not initialized. Please set an instance of EventBus.')
        }
    }

    static Correlation subscribeCorrelation(Correlation correlation) {
        assertEventBusIsInitialized()
        INSTANCE.doSubscribeCorrelation(correlation)
    }

    abstract Correlation doSubscribeCorrelation(Correlation correlation)


    static void emit(UUID correlation, String eventType) {
        assertEventBusIsInitialized()
        INSTANCE.doEmit(correlation, eventType)
    }

    abstract void doEmit(UUID correlation, String eventType)

}
