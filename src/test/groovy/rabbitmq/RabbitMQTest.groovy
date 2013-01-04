package rabbitmq

import com.rabbitmq.client.*
import infrastructure.messaging.AMQPConstants
import org.junit.*

import static infrastructure.messaging.AMQPConstants.AUTO_DELETE
import static infrastructure.messaging.AMQPConstants.NOT_DURABLE
import static infrastructure.messaging.AMQPConstants.NOT_EXCLUSIVE
import static infrastructure.messaging.AMQPConstants.NO_ADDITIONAL_ARGUMENTS
import static org.hamcrest.CoreMatchers.notNullValue
import static org.hamcrest.CoreMatchers.startsWith
import static org.junit.Assert.assertThat

class RabbitMQ_ExchangeLifecycle_Test {}

class RabbitMQ_QueueLifecycle_Test {


    Channel channel

    @Before
    public void setUp() throws Exception {
        ConnectionFactory connectionFactory = new ConnectionFactory()
        Connection connection = connectionFactory.newConnection()
        channel = connection.createChannel()
    }

    @Test
    public void declare_a_new_queue() {
        final AMQP.Queue.DeclareOk declareOk = channel.queueDeclare()

        assertThat declareOk.queue, startsWith('amq.gen-')
    }

    @Test
    public void declare_an_existing_queue() {
        final AMQP.Queue.DeclareOk firstDeclareOk = channel.queueDeclare()
        final AMQP.Queue.DeclareOk secondDeclareOk = channel.queueDeclare(firstDeclareOk.queue, NOT_DURABLE, NOT_EXCLUSIVE, AUTO_DELETE, NO_ADDITIONAL_ARGUMENTS)

        assertThat secondDeclareOk, notNullValue()
    }

    @Test
    public void delete_an_existing_queue() {

    }

}

class RabbitMQ_Multithreaded_Test {}

class RabbitMQ_ConsumerLifecycle_Test {

    @Test
    public void learn_about_lifecycle() {

    }
}

class RabbitMQ_ProducerLifecycle_Test {

    @Test
    public void learn_about_lifecycle() {

    }
}

class RabbitMQ_Binding_Test {

    @Test
    public void learn_about_lifecycle() {

    }
}

class RabbitMQ_Routing_Test {

    @Test
    public void learn_about_lifecycle() {

    }
}
