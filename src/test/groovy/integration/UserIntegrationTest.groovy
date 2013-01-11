package integration

import domain.commands.Register_user
import org.junit.*
import readmodels.*

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
    void should_build_user_summary_from_command() {
        final userId = randomUUID()
        commandRouter.route(new Register_user(
            newUserUUID: userId,
            firstName: 'Andreas',
            lastName: 'Simon',
            eMail: 'a.simon@one-os.de',
            password: 'optimus'
        ))

        Thread.sleep(100)

        final allUserSummaries = readModelRepository.getAll(UserSummary)
        assertThat allUserSummaries, equalTo([new UserSummary(userId, 'Andreas', 'Simon', 'a.simon@one-os.de')])
    }

    @Test
    void should_build_login_from_command() {
        final userId = randomUUID()
        commandRouter.route(new Register_user(
            newUserUUID: userId,
            firstName: 'Andreas',
            lastName: 'Simon',
            eMail: 'a.simon@one-os.de',
            password: 'optimus'
        ))

        Thread.sleep(100)

        final allLogins = readModelRepository.getAll(Login)
        assertThat allLogins, equalTo([new Login(userId, 'a.simon@one-os.de', 'optimus')])
    }

}
