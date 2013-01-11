package integration

import com.rabbitmq.client.ConnectionFactory
import com.rabbitmq.client.impl.AMQConnection
import domain.commands.CommandRouter
import infrastructure.*
import infrastructure.messaging.AMQPEventPublisher
import org.postgresql.ds.PGSimpleDataSource
import org.springframework.jdbc.core.JdbcTemplate
import readmodels.*
import readmodels.eventhandlers.*

import javax.sql.DataSource

abstract class IntegrationTest {

    private AMQConnection amqpConnection
    private DataSource dataSource
    private JdbcTemplate jdbcTemplate
    private Repository repository
    private CommandRouter commandRouter
    private ReadModelBuilder readModelBuilder
    private ReadModelRepository readModelRepository



    ReadModelBuilder getReadModelBuilder() {
        if (readModelBuilder) { return readModelBuilder }

        readModelBuilder = new ReadModelBuilder(getAmqpConnection())
        readModelBuilder.eventHandlers = [
            new User_was_registered_Handler(),
            new New_device_was_registered_Handler(),
            new Device_was_locked_out_Handler(),
            new Device_was_unregistered_Handler()
        ]
        readModelBuilder.jdbcTemplate = getJdbcTemplate()
        return readModelBuilder
    }

    ReadModelRepository getReadModelRepository() {
        if (readModelRepository) { return readModelRepository }

        readModelRepository = new ReadModelRepository(jdbcTemplate)
        readModelRepository
    }

    CommandRouter getCommandRouter() {
        if(commandRouter) { return commandRouter }
        commandRouter = new CommandRouter()
        commandRouter.eventPublisher = new AMQPEventPublisher(getAmqpConnection())
        commandRouter.repository = getRepository()
        commandRouter
    }

    AMQConnection getAmqpConnection() {
        if (amqpConnection) {
            return amqpConnection
        }
        def amqpConnectionFactory = new ConnectionFactory(virtualHost: 'one-os-test')
        amqpConnection = amqpConnectionFactory.newConnection()
        return amqpConnection
    }

    DataSource getDataSource() {
        if (dataSource) { return dataSource }

        dataSource = new PGSimpleDataSource()
        dataSource.user = 'user12'
        dataSource.password = '34klq*'
        dataSource.databaseName = 'one-os-cqrs'
        dataSource
    }

    JdbcTemplate getJdbcTemplate() {
        if (jdbcTemplate) { return jdbcTemplate }

        jdbcTemplate = new JdbcTemplate(getDataSource())
        jdbcTemplate
    }

    Repository getRepository() {
        if (repository) { return repository }
        repository = new JdbcRepository(jdbcTemplate: getJdbcTemplate())
        repository
    }

    void createTables(JdbcTemplate jdbcTemplate) {
        jdbcTemplate.execute(
            'CREATE TABLE Events (' +
            '    AggregateClassName varchar(255) NOT NULL,' +
            '    AggregateId uuid NOT NULL,' +
            '    EventName varchar(255) NOT NULL,' +
            '    Attributes text NOT NULL,' +
            '    Timestamp timestamp NOT NULL' +
            ');'
        )
        jdbcTemplate.execute(
            'CREATE TABLE DeviceSummary (' +
            '    deviceId uuid PRIMARY KEY,' +
            '    deviceName VARCHAR(25)' +
            ');'
        )
        jdbcTemplate.execute(
            'CREATE TABLE UserSummary (' +
            '    userId uuid PRIMARY KEY,' +
            '    firstName VARCHAR(255),' +
            '    lastName  VARCHAR(255),' +
            '    eMail         VARCHAR(255),' +
            '    htmlTableRow  TEXT' +
            ');'
        )
        jdbcTemplate.execute(
            'CREATE TABLE Login (' +
            '    userId uuid PRIMARY KEY,' +
            '    eMail       VARCHAR(255),' +
            '    password    VARCHAR(255)' +
            ');'
        )
    }

    void dropTables(JdbcTemplate jdbcTemplate) {
        jdbcTemplate.execute('DROP TABLE IF EXISTS Events;')
        jdbcTemplate.execute('DROP TABLE IF EXISTS DeviceSummary;')
        jdbcTemplate.execute('DROP TABLE IF EXISTS UserSummary;')
        jdbcTemplate.execute('DROP TABLE IF EXISTS Login;')
    }

}
