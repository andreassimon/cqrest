package org.cqrest.eventstore.inmemory

import org.cqrest.eventsourcing.EventEnvelope


class TotallyDefectiveObserver implements org.cqrest.reactive.Observer<EventEnvelope> {

    public static final RuntimeException THROWN_BY_ON_NEXT = new RuntimeException('Thrown by TotallyDefectiveObserver#onNext(EventEnvelope envelope)')
    public static final RuntimeException THROWN_BY_ON_ERROR = new RuntimeException('Thrown by TotallyDefectiveObserver#onError(Throwable e)')
    public static final RuntimeException THROWN_BY_ON_COMPLETED = new RuntimeException('Thrown by TotallyDefectiveObserver#onCompleted()')

    @Override
    void onNext(EventEnvelope envelope) {
        throw THROWN_BY_ON_NEXT
    }

    @Override
    void onError(Throwable e) {
        throw THROWN_BY_ON_ERROR
    }

    @Override
    void onCompleted() {
        throw THROWN_BY_ON_COMPLETED
    }

}
