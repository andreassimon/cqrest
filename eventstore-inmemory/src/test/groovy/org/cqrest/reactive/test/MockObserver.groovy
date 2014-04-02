package org.cqrest.reactive.test

import de.oneos.eventsourcing.EventEnvelope
import org.junit.Assert

import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit


class MockObserver implements org.cqrest.reactive.Observer<EventEnvelope> {

    Iterator<EventEnvelope> expectedEnvelopes
    CountDownLatch latch
    List<Throwable> errors = []

    MockObserver(List<EventEnvelope> expected) {
        expectedEnvelopes = expected.asImmutable().iterator()
        latch = new CountDownLatch(expected.size())
    }

    @Override
    void onCompleted() {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    void onError(Throwable throwable) {
        errors << throwable
    }

    @Override
    void onNext(EventEnvelope actualNextEnvelope) {
        assert expectedEnvelopes.next() == actualNextEnvelope
        latch.countDown()
    }

    void assertReceivedEvents() {
        if(!latch.await(500L, TimeUnit.MILLISECONDS)) {
            if(!errors.empty) {
                throw errors[0]
            }
            Assert.fail("The expected items were not passed within timeout limit")
        }
    }
}
