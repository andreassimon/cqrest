package rabbitmq

import com.rabbitmq.client.*
import org.junit.*

import static infrastructure.messaging.AMQPConstants.*
import static org.hamcrest.CoreMatchers.*
import static org.junit.Assert.assertThat


class RabbitMQ_ExchangeLifecycle_Test {

    Channel channel


    @Before
    void setUp() throws Exception {
        ConnectionFactory connectionFactory = new ConnectionFactory()
        Connection connection = connectionFactory.newConnection()
        channel = connection.createChannel()
    }


    @Test
    void create_a_new_direct_exchange() {
        final AMQP.Exchange.DeclareOk declareOk = channel.exchangeDeclare('new-direct-exchange', DIRECT_EXCHANGE)

        assertThat declareOk, notNullValue()
    }

    @Test
    void create_a_new_fanout_exchange() {
        final AMQP.Exchange.DeclareOk declareOk = channel.exchangeDeclare('new-fanout-exchange', FANOUT_EXCHANGE)

        assertThat declareOk, notNullValue()
    }

    @Test
    void create_a_new_topic_exchange() {
        final AMQP.Exchange.DeclareOk declareOk = channel.exchangeDeclare('new-topic-exchange', TOPIC_EXCHANGE)

        assertThat declareOk, notNullValue()
    }
}

class RabbitMQ_QueueLifecycle_Test {

    Channel channel


    @Before
    void setUp() throws Exception {
        ConnectionFactory connectionFactory = new ConnectionFactory()
        Connection connection = connectionFactory.newConnection()
        channel = connection.createChannel()
    }

    @Test
    void declare_a_new_temporary_queue() {
        final AMQP.Queue.DeclareOk declareOk = channel.queueDeclare()

        assertThat declareOk.queue, startsWith('amq.gen-')
    }

    @Test
    void declare_a_named_queue() {
        final AMQP.Queue.DeclareOk declareOk = channel.queueDeclare('explicitly-named-queue', NOT_DURABLE, EXCLUSIVE, AUTO_DELETE, NO_ADDITIONAL_ARGUMENTS)

        assertThat declareOk, notNullValue()
    }

    @Test(expected = IOException.class)
    void queue_name_must_not_start_with_amq() {
        final RESERVED_AMQP_PREFIX = 'amq.'
        final ILLEGAL_QUEUE_NAME = RESERVED_AMQP_PREFIX + 'gen-wxvNnJM-T8bSSFsx5rkCNj'
        channel.queueDeclare(ILLEGAL_QUEUE_NAME, NOT_DURABLE, NOT_EXCLUSIVE, AUTO_DELETE, NO_ADDITIONAL_ARGUMENTS)
    }

    @Test
    void delete_an_existing_queue() {
        final AMQP.Queue.DeclareOk declareOk = channel.queueDeclare()

        final AMQP.Queue.DeleteOk deleteOk = channel.queueDelete(declareOk.queue)

        assertThat deleteOk, notNullValue()
    }

}

class RabbitMQ_Multithreaded_Test { }

class RabbitMQ_ConsumerLifecycle_Test { }

class RabbitMQ_ProducerLifecycle_Test { }

class RabbitMQ_Binding_Test { }

class RabbitMQ_Routing_Test { }
