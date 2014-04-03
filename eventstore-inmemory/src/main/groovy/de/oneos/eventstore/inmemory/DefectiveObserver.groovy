package de.oneos.eventstore.inmemory

import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit

import de.oneos.eventsourcing.EventEnvelope


class DefectiveObserver implements org.cqrest.reactive.Observer<EventEnvelope> {
    public static final RuntimeException THROWN_BY_ON_NEXT = new RuntimeException('Thrown by DefectiveObserver#onNext(EventEnvelope envelope)')
    final CountDownLatch latch = new CountDownLatch(1)

    @Override
    void onNext(EventEnvelope envelope) {
        throw THROWN_BY_ON_NEXT
    }

    @Override
    void onError(Throwable e) {
        assert THROWN_BY_ON_NEXT == e
        latch.countDown()
    }

    @Override
    void onCompleted() {
        // Not relevant to the test
    }

    void assertReceivedExceptionIn_onError() {
        assert latch.await(500L, TimeUnit.MILLISECONDS)
    }

}
