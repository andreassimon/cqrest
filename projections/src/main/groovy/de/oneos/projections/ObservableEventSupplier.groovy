package de.oneos.projections

import de.oneos.eventsourcing.EventEnvelope
import de.oneos.eventsourcing.EventSupplier
import rx.Subscription
import rx.lang.groovy.GroovyOnSubscribeFuncWrapper


class ObservableEventSupplier implements EventSupplier{

    @Delegate
    EventSupplier wrappee

    ObservableEventSupplier(EventSupplier wrappee) {
        this.wrappee = wrappee
    }

    @Override
    @SuppressWarnings("GroovyAssignabilityCheck")
    Observable<EventEnvelope> observe(Map<String, ?> criteria) {
        return new Observable<EventEnvelope>(
            rx.Observable.create(new GroovyOnSubscribeFuncWrapper<EventEnvelope>({ rx.Observer<EventEnvelope> observer ->
                deliverEvents criteria, observer.&onNext

                withEventEnvelopes criteria, observer.&onNext

                return new Subscription() {
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
