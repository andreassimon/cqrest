package de.oneos.eventstore

import org.apache.commons.logging.Log

import de.oneos.eventsourcing.EventEnvelope


class Subscribers implements rx.Observable.OnSubscribeFunc<EventEnvelope> {

    private Log log

    Collection<rx.Observer<? super EventEnvelope>> observers = []
    private rx.Observable<EventEnvelope> observable = rx.Observable.create(this)


    Subscribers(Log log) {
        this.log = log
    }


    @Override
    rx.Subscription onSubscribe(rx.Observer<? super EventEnvelope> observer) {
        observers.add(observer)
        return [
          unsubscribe: { observers.remove(observer) }
        ] as rx.Subscription
    }

    public rx.Observable<EventEnvelope> getObservable() {
        return observable
    }


    void publish(eventEnvelope) {
        new ArrayList(observers).each { rx.Observer observer ->
            try {
                observer.onNext(eventEnvelope)
            } catch(e) {
                try {
                    observer.onError(e)
                } catch(ee) {
                    log.error("${ee.getClass().getCanonicalName()}: Couldn't process $eventEnvelope in $observer", ee)
                }
            }
        }
    }

}
