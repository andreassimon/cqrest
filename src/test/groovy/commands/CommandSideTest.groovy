package commands

import domain.commands.CommandRouter
import org.junit.Before
import utilities.InMemoryEventPublisher
import utilities.InMemoryEventStore

import static org.hamcrest.CoreMatchers.equalTo
import static org.junit.Assert.assertThat

abstract class CommandSideTest {
    def eventStore = new InMemoryEventStore()
    def eventPublisher = new InMemoryEventPublisher()
    def commandRouter

    @Before
    public void setUp() throws Exception {
        commandRouter = new CommandRouter()

        commandRouter.eventStore = eventStore
        commandRouter.eventPublisher = eventPublisher
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
        assertThat(eventPublisher.receivedEventEnvelopes, equalTo(expectedEvents))
    }

    def collectEventsFrom(Closure closure) {
        def eventCollector = new EventCollector()
        eventCollector.with closure
        eventCollector.toList()
    }
}

