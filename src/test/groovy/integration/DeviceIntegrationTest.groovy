package integration

import com.rabbitmq.client.ConnectionFactory
import domain.commands.*
import infrastructure.messaging.AMQPEventPublisher
import org.junit.*
import org.postgresql.ds.PGSimpleDataSource
import org.springframework.jdbc.core.JdbcTemplate
import readmodels.*
import readmodels.eventhandlers.*
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
        def factory = new ConnectionFactory()
        factory.virtualHost = 'one-os-test'
        def connection = factory.newConnection()

        def amqpEventPublisher = new AMQPEventPublisher(connection)
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
        readModelBuilder = new ReadModelBuilder(connection)
        readModelBuilder.eventHandlers = [
                new New_user_was_created_Handler(),
                new New_device_was_registered_Handler(),
                new Device_was_locked_out_Handler(),
                new Device_was_unregistered_Handler()
        ]
        readModelBuilder.jdbcTemplate = jdbcTemplate
        readModelBuilder.start()
    }

    @Test
    void should_build_read_model_from_command() {
        final deviceId = UUID.randomUUID()
        final deviceName = 'new device'
        commandRouter.route new Register_new_device(deviceId: deviceId, deviceName: deviceName)

        Thread.sleep(100)

        final allDeviceSummaries = readModelRepository.getAll(DeviceSummary.class)
        assertThat allDeviceSummaries, equalTo([new DeviceSummary(deviceId, deviceName)])
    }



    @After
    public void tearDown() throws Exception {
        readModelBuilder.interrupt()
    }
}
