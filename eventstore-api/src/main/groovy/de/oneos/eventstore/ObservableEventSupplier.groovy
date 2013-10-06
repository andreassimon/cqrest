package de.oneos.eventstore


abstract class ObservableEventSupplier implements EventSupplier{

    @Override
    @SuppressWarnings("GroovyAssignabilityCheck")
    rx.Observable<EventEnvelope> observe(Map<String, ?> criteria) {
        return rx.Observable.create({ rx.Observer<EventEnvelope> observer ->
            deliverEvents criteria, observer.&onNext

            withEventEnvelopes criteria, observer.&onNext

            return new rx.Subscription() {
                @Override
                void unsubscribe() {
                    // TODO implement
                }
            }
        })
    }

    protected void deliverEvents(Map<String, ? extends Object> criteria, Closure callback) {
        subscribeTo(new ClosureEventConsumer(criteria, callback))
    }


}
