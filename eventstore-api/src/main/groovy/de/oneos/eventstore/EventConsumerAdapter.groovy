package de.oneos.eventstore

import de.oneos.eventsourcing.EventConsumer
import de.oneos.eventsourcing.EventEnvelope


class EventConsumerAdapter implements rx.Observer<EventEnvelope> {

    final EventConsumer wrappee

    EventConsumerAdapter(EventConsumer wrappee) {
        assert wrappee
        this.wrappee = wrappee
    }

    @Override
    void onNext(EventEnvelope envelope) {
        wrappee.process(envelope)
    }

    @Override
    void onCompleted() {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    void onError(Throwable e) {
        //To change body of implemented methods use File | Settings | File Templates.
    }

}
