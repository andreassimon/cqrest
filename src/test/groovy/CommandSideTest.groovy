import org.junit.Before

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

    void when(command) {
        commandRouter.route(command)
    }

    void then(List expectedEvents) {
        assertThat(eventPublisher.receivedEvents, equalTo(expectedEvents))
    }
}
