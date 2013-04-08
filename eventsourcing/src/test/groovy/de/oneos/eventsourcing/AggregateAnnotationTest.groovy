package de.oneos.eventsourcing

import static java.util.UUID.randomUUID

import org.junit.*
import static org.junit.Assert.*
import static org.hamcrest.Matchers.*
import de.oneos.eventsourcing.orders.Order
import de.oneos.eventsourcing.orders.Order_line_was_added
import de.oneos.eventsourcing.orders.Order_was_created


@Ignore
class AggregateAnnotationTest {

    UUID generatedOrderId = randomUUID()
    UUID article1 = randomUUID()
    UUID article2 = randomUUID()

    @Test
    void instance_method__emit__should_return_constant_string() {
        def order = new Order(randomUUID())

        assertThat order.emit( new Order_line_was_added(article1) ), equalTo('emit called')
    }

    @Test
    void instance_method__emit__should_collect_emitted_events() {
//        def order = Order.create(generatedOrderId)
        def order = new Order(randomUUID())

        order.emit( new Order_line_was_added(article1) )
//        order.addArticles([article1, article2])

        assertThat order.newEvents, equalTo([
            new Order_was_created(),
            new Order_line_was_added(article1),
            new Order_line_was_added(article2)
        ])
    }

    @Test
    void instance_method__emit__should_apply_the_event_to_the_emitting_instance_immediately() {
        def order = new Order(randomUUID())

        order.emit( new Order_line_was_added(article1) )
//        order.addArticles([article1, article2])

        assertThat order.orderLines, equalTo([article1, article2])
    }

    @Test
    void instance_method__flushEvents__should_clear_the_event_list() {
        def order = new Order(randomUUID())
        order.addArticles([article1, article2])

        order.flushEvents()

        assertThat order.newEvents, empty()
    }

}




