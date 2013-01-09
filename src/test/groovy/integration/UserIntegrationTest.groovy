package integration

import domain.commands.Register_new_user
import org.junit.*
import readmodels.UserSummary

import static java.util.UUID.randomUUID
import static org.hamcrest.Matchers.equalTo
import static org.junit.Assert.assertThat

class UserIntegrationTest extends IntegrationTest {

    @Before
    public void setUp() throws Exception {
        dropTables(jdbcTemplate)
        createTables(jdbcTemplate)
        readModelBuilder.start()
    }

    @After
    public void tearDown() throws Exception {
        dropTables(jdbcTemplate)
        readModelBuilder.interrupt()
    }

    @Test
    void should_build_read_model_from_command() {
        final userId = randomUUID()
        commandRouter.route(new Register_new_user(newUserUUID: userId, firstName: 'Andreas', lastName: 'Simon', eMail: 'a.simon@one-os.de'))

        Thread.sleep(100)

        final allUserSummaries = readModelRepository.getAll(UserSummary)
        assertThat allUserSummaries, equalTo([new UserSummary(userId, 'Andreas', 'Simon', 'a.simon@one-os.de')])
    }

}
