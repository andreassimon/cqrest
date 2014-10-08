package org.cqrest.eventsourcing

import static java.util.UUID.randomUUID

import static org.junit.Assert.*
import static org.hamcrest.Matchers.*
import org.cqrest.eventsourcing.orders.Order_line_was_added
import org.cqrest.eventsourcing.orders.Order_was_created


class AggregateAnnotationTest extends GroovyTestCase {

    UUID generatedOrderId = randomUUID()
    UUID article1 = randomUUID()
    UUID article2 = randomUUID()

        def orderSource = '''
package org.cqrest.eventsourcing.orders


@org.cqrest.eventsourcing.Aggregate
class Order {

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

    def "Order was created"(Map attributes) { }

    def "Order line was added"(Map orderLine) {
        orderLines << UUID.fromString(orderLine['article'])
    }
}
'''

    GroovyClassLoader invoker = new GroovyClassLoader()
    Class orderClazz


    void setUp() {
        super.setUp()

        orderClazz = invoker.parseClass(orderSource)
    }

    void test__instance_method__emit__should_return_the_instance_for_chaining() {
        def order = orderClazz.create(generatedOrderId)

        final actual = order.emit(new Order_line_was_added(article1))

        assertThat actual, equalTo(order)
    }

    void test__instance_method__emit__should_collect_emitted_events() {
        def order = orderClazz.create(generatedOrderId)

        order.emit(new Order_line_was_added(article1))
        order.emit(new Order_line_was_added(article2))

        assertThat order.newEvents, equalTo([
            new Order_was_created(),
            new Order_line_was_added(article1),
            new Order_line_was_added(article2)
        ])
    }

    void test__instance_method__emit__should_apply_the_event_to_the_emitting_instance_immediately() {
        def order = orderClazz.create(generatedOrderId)

        order.emit(new Order_line_was_added(article1))
        order.emit(new Order_line_was_added(article2))

        assertThat order.orderLines, equalTo([article1, article2])
    }

    void test__instance_method__flushEvents__should_clear_the_event_list() {
        def order = orderClazz.create(generatedOrderId)
        order.emit(new Order_line_was_added(article1))
        order.emit(new Order_line_was_added(article2))
        assertThat order.newEvents, not(empty())

        order.flushEvents()

        assertThat order.newEvents, empty()
    }

    void test__a_static_attribute_aggregateName_is_derived_from_the_class_name() {
        assert orderClazz.aggregateName == 'Order'
        assert orderClazz.getAggregateName() == 'Order'
        DefaultAggregateFactory.assertAggregateNameIsDefined(orderClazz)
    }

    void test__keeps_an_existing_aggregateName_field_as_is() {
        orderClazz = invoker.parseClass('''
@org.cqrest.eventsourcing.Aggregate
class Order {

    static aggregateName = 'Auftrag'

    final UUID id

    Order(UUID id) {
        this.id = id
    }

}
''')

        assert orderClazz.aggregateName == 'Auftrag'
        assert orderClazz.getAggregateName() == 'Auftrag'
    }

    void test__keeps_an_existing_String_aggregateName_field_as_is() {
        orderClazz = invoker.parseClass('''
@org.cqrest.eventsourcing.Aggregate
class Order {

    static String aggregateName = 'Auftrag'

    final UUID id

    Order(UUID id) {
        this.id = id
    }

}
''')

        assert orderClazz.aggregateName == 'Auftrag'
        assert orderClazz.getAggregateName() == 'Auftrag'
    }

    void test__adopts_the_aggregateName_from_superclasses() {
        orderClazz = invoker.parseClass('''
@org.cqrest.eventsourcing.Aggregate
class Order {

    static String aggregateName = 'Auftrag'

    final UUID id

    Order(UUID id) {
        this.id = id
    }

}
''')

        def orderSubclazz = invoker.parseClass('''
@org.cqrest.eventsourcing.Aggregate
class OrderSubclazz extends Order{

    final UUID id

    OrderSubclazz(UUID id) {
        this.id = id
    }

}
''')
        assert orderSubclazz.aggregateName == 'Auftrag'
        assert orderSubclazz.getAggregateName() == 'Auftrag'
    }

}
