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
        def order = Order.create(generatedOrderId)

        order.addArticles([article1, article2])

        assertThat order.newEvents, equalTo([
            new Order_was_created(),
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

    @Test
    void instance_method__flushEvents__should_clear_the_event_list() {
        def order = new Order(randomUUID())
        order.addArticles([article1, article2])

        order.flushEvents()

        assertThat order.newEvents, empty()
    }



    static class Order {
        static { Order.mixin(EventSourcing) }

        static applicationName = 'APPLICATION'
        static boundedContextName = 'BOUNDED CONTEXT'
        static aggregateName = 'AGGREGATE'

        final UUID id
        def orderLines = []

        static Order create(UUID id) {
            assert id != null
            Order newOrder = new Order(id)
            newOrder.emit(
                new Order_was_created()
            )
            return newOrder
        }

        Order(UUID id) {
            this.id = id
        }

        void addArticles(List<UUID> articles) {
            emit(
                *articles.collect { new Order_line_was_added(it) }
            )
        }
    }

    static class Order_was_created extends Event<Order> {
        void applyTo(Order order) {
            order
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
