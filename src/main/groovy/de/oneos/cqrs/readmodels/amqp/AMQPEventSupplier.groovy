package de.oneos.cqrs.readmodels.amqp

import com.rabbitmq.client.Channel
import de.oneos.cqrs.readmodels.EventFilter
import de.oneos.cqrs.readmodels.EventProcessor
import de.oneos.cqrs.readmodels.EventSupplier

class AMQPEventSupplier implements EventSupplier {
    Channel channel
    String queueName

    static String routingKey(EventFilter eventFilter) {
        eventFilter.withConstrainedValues(['applicationName', 'boundedContextName', 'aggregateName', 'eventName']) { constrainedValues ->
            constrainedValues.collect { it ?: '*' }.join('.')
        }
    }

    void setChannel(Channel channel) {
        this.channel = channel
        declareQueue(channel)
    }

    void declareQueue(Channel channel) {
        def declareOk = channel.queueDeclare()
        this.queueName = declareOk.queue
    }

    @Override
    void subscribeTo(EventFilter eventFilter, EventProcessor eventProcessor) {
        channel.queueBind(this.queueName, AMQPConstants.EVENT_EXCHANGE_NAME, routingKey(eventFilter))
    }
}
