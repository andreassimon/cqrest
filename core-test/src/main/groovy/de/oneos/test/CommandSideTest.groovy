package de.oneos.test

import org.junit.*

import de.oneos.eventstore.inmemory.*


abstract class CommandSideTest {
    def eventStore = new InMemoryEventStore()
    def commandRouter

    @Before
    public void setUp() throws Exception {
//        commandRouter = new CommandRouter()
//
//        commandRouter.eventStore = eventStore
//        commandRouter.eventPublisher = eventPublisher
    }

    void given(Closure history) {
        given collectEventsFrom(history)
    }

    void given(List history) {
        eventStore.history = history
    }

    void when(command) {
        commandRouter.route(command)
    }

    void then(Closure expectedEvents) {
        then collectEventsFrom(expectedEvents)
    }

    void then(List expectedEvents) {
        org.junit.Assert.assertThat(eventPublisher.receivedEventEnvelopes, org.hamcrest.CoreMatchers.equalTo(expectedEvents))
    }

    def collectEventsFrom(Closure closure) {
//        def eventCollector = new EventCollector()
        eventCollector.with closure
        eventCollector.toList()
    }
}

