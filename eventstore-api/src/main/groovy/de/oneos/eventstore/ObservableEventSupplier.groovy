package de.oneos.eventstore

import rx.lang.groovy.GroovyOnSubscribeFuncWrapper


abstract class ObservableEventSupplier implements EventSupplier{

    @Override
    @SuppressWarnings("GroovyAssignabilityCheck")
    de.oneos.projections.Observable<EventEnvelope> observe(Map<String, ?> criteria) {
        return new de.oneos.projections.Observable<EventEnvelope>(
            rx.Observable.create(new GroovyOnSubscribeFuncWrapper<EventEnvelope>({ rx.Observer<EventEnvelope> observer ->
                deliverEvents criteria, observer.&onNext

                withEventEnvelopes criteria, observer.&onNext

                return new rx.Subscription() {
                    @Override
                    void unsubscribe() {
                        // TODO implement
                    }
                }
            }))
        )
    }

    protected void deliverEvents(Map<String, ? extends Object> criteria, Closure callback) {
        subscribeTo(new ClosureEventConsumer(criteria, callback))
    }


}
