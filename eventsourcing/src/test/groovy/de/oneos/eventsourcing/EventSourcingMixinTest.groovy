package de.oneos.eventsourcing

import static java.util.UUID.randomUUID

import org.junit.*
import static org.junit.Assert.*
import static org.hamcrest.Matchers.*


class EventSourcingMixinTest {

    UUID generatedOrderId = randomUUID()
    UUID article1 = randomUUID()
    UUID article2 = randomUUID()

    @Test
    void instance_method__emit__should_collect_emitted_events() {
        def aggregate = new Order(generatedOrderId)

        aggregate.addArticles([article1, article2])

        assertThat aggregate.newEvents, equalTo([
            new Order_was_created(generatedOrderId),
            new Order_line_was_added(article1),
            new Order_line_was_added(article2)
        ])
    }

    @Test
    void instance_method__emit__should_apply_the_event_to_the_emitting_instance_immediately() {
        def order = new Order(randomUUID())

        order.addArticles([article1, article2])

        assertThat order.orderLines, equalTo([article1, article2])
    }



    static class Order {
        static { Order.mixin(EventSourcing) }

        static applicationName = 'APPLICATION'
        static boundedContextName = 'BOUNDED CONTEXT'
        static aggregateName = 'AGGREGATE'

        UUID id

        def orderLines = []

        Order(UUID id) {
            emit(
                new Order_was_created(id)
            )
        }

        void addArticles(List<UUID> articles) {
            emit(
                *articles.collect { new Order_line_was_added(it) }
            )
        }
    }

    static class Order_was_created extends Event<Order> {
        UUID aggregateId

        Order_was_created(UUID aggregateId) {
            this.aggregateId = aggregateId
        }

        void applyTo(Order aggregate) {
            aggregate.id = aggregateId
        }
    }

    static class Order_line_was_added extends Event<Order> {
        UUID article

        Order_line_was_added(UUID article) {
            this.article = article
        }

        @Override
        void applyTo(Order order) {
            order.orderLines << article
        }
    }

}
