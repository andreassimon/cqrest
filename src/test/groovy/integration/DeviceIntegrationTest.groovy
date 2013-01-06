package integration

import com.rabbitmq.client.ConnectionFactory
import domain.commands.CommandRouter
import domain.commands.Register_new_device
import infrastructure.messaging.AMQPEventPublisher
import org.junit.*
import org.postgresql.ds.PGSimpleDataSource
import org.springframework.jdbc.core.JdbcTemplate
import readmodels.DeviceSummary
import readmodels.ReadModelBuilder
import readmodels.ReadModelRepository
import utilities.InMemoryRepository

import javax.sql.DataSource

import static org.hamcrest.Matchers.equalTo
import static org.junit.Assert.assertThat

class DeviceIntegrationTest {

    CommandRouter commandRouter
    ReadModelRepository readModelRepository
    ReadModelBuilder readModelBuilder

    @Before
    public void setUp() throws Exception {
        def amqpEventPublisher = new AMQPEventPublisher()
        def inMemoryRepository = new InMemoryRepository()

        commandRouter = new CommandRouter()
        commandRouter.eventPublisher = amqpEventPublisher
        commandRouter.repository     = inMemoryRepository

        DataSource dataSource = new PGSimpleDataSource()
        dataSource.user = 'user12'
        dataSource.password = '34klq*'
        dataSource.databaseName = 'one-os-cqrs'

        JdbcTemplate jdbcTemplate = new JdbcTemplate(dataSource)

        jdbcTemplate.execute('DROP TABLE IF EXISTS DeviceSummary')
        jdbcTemplate.execute(
            'CREATE TABLE IF NOT EXISTS DeviceSummary (' +
            '    deviceId uuid PRIMARY KEY,' +
            '    deviceName VARCHAR(25)' +
            ');'
        )

        readModelRepository = new ReadModelRepository(jdbcTemplate)
        readModelBuilder = ReadModelBuilder.newInstance(jdbcTemplate)
        readModelBuilder.start()
    }

    @Test
    void should_build_read_model_from_command() {
        final deviceId = UUID.randomUUID()
        final deviceName = 'new device'
        commandRouter.route new Register_new_device(deviceId, deviceName)

        Thread.sleep(100)

        final allDeviceSummaries = readModelRepository.getAll(DeviceSummary.class)
        assertThat allDeviceSummaries, equalTo([new DeviceSummary(deviceId, deviceName)])
    }



    @After
    public void tearDown() throws Exception {
        readModelBuilder.interrupt()
    }
}
