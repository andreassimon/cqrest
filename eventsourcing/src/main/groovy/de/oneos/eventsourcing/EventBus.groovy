package de.oneos.eventsourcing

import org.apache.commons.logging.*


abstract class EventBus {

    public static Log log = LogFactory.getLog(this)

    protected static EventBus INSTANCE

    static void setINSTANCE(EventBus instance) {
        INSTANCE = instance
    }


    static Correlation subscribeCorrelation(Correlation correlation) {
        INSTANCE.doSubscribeCorrelation(correlation)
    }

    abstract Correlation doSubscribeCorrelation(Correlation correlation)


    static void emit(UUID correlation, String eventType) {
        INSTANCE.doEmit(correlation, eventType)
    }

    abstract void doEmit(UUID correlation, String eventType)

}
