package org.cqrest.eventsourcing

import rx.Subscription
import rx.lang.groovy.GroovyOnSubscribeFuncWrapper


class ObservableEventSupplier implements EventSupplier, EventStream {

    @Delegate
    EventSupplier wrappee

    ObservableEventSupplier(EventSupplier wrappee) {
        this.wrappee = wrappee
    }

    // TODO Move to implementors, esp. EventStore, AMQPEventSupplier
    @SuppressWarnings("GroovyAssignabilityCheck")
    org.cqrest.reactive.Observable<EventEnvelope> observe(Map<String, ?> criteria) {
        return new org.cqrest.reactive.Observable<EventEnvelope>(
            rx.Observable.create(new GroovyOnSubscribeFuncWrapper<EventEnvelope>({ rx.Observer<EventEnvelope> observer ->
                wrappee.subscribeTo(criteria, new ClosureEventConsumer(criteria, observer.&onNext))

                wrappee.withEventEnvelopes criteria, observer.&onNext

                return new Subscription() {
                    @Override
                    void unsubscribe() {
                        // TODO implement
                    }
                }
            }))
        )
    }

}
