package commands

import domain.commands.CommandRouter
import org.junit.Before
import utilities.InMemoryEventPublisher
import utilities.InMemoryRepository

import static org.hamcrest.CoreMatchers.equalTo
import static org.junit.Assert.assertThat

abstract class CommandSideTest {
    def repository = new InMemoryRepository()
    def eventPublisher = new InMemoryEventPublisher()
    def commandRouter

    @Before
    public void setUp() throws Exception {
        commandRouter = new CommandRouter()

        commandRouter.repository = repository
        commandRouter.eventPublisher = eventPublisher
    }

    void given(Closure history) {
        given collectEventsFrom(history)
    }

    void given(List history) {
        repository.history = history
    }

    void when(command) {
        commandRouter.route(command)
    }

    void then(Closure expectedEvents) {
        then collectEventsFrom(expectedEvents)
    }

    void then(List expectedEvents) {
        assertThat(eventPublisher.receivedEvents, equalTo(expectedEvents))
    }

    def collectEventsFrom(Closure closure) {
        def eventCollector = new EventCollector()
        eventCollector.with closure
        eventCollector.toList()
    }
}

class EventCollector {
    def eventList = []

    def methodMissing(String eventName, arguments) {
        eventList << Class.forName("domain.events.$eventName").newInstance(arguments)
    }

    def toList() {
        eventList
    }
}
