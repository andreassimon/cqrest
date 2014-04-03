package de.oneos.eventstore.inmemory

import org.junit.Test

import org.cqrest.test.AnEventEnvelope
import static org.cqrest.test.AnEventEnvelope.anEventEnvelope


class CriteriaFilterTest {

    @Test
    void test_should_be_truthy_when_event_name_is_equal() {
        CriteriaFilter filter = new CriteriaFilter(eventName: 'An event happened')

        assert filter.test(anEventEnvelope().withEventName('An event happened').build())
    }

    @Test
    void test_should_be_truthy_when_event_name_is_member_of_criteria() {
        CriteriaFilter filter = new CriteriaFilter(eventName: ['An event happened', 'Another event happened'])

        assert filter.test(anEventEnvelope().withEventName('An event happened').build())
    }

    @Test
    void test_should_be_falsy_when_event_names_are_not_equal() {
        CriteriaFilter filter = new CriteriaFilter(eventName: 'An event happened')

        assert false == filter.test(anEventEnvelope().withEventName('Another event happened').build())
    }

    @Test
    void test_should_be_truthy_when_aggregate_names_are_equal() {
        CriteriaFilter filter = new CriteriaFilter(aggregateName: 'Order')

        assert filter.test(anEventEnvelope().withAggregateName('Order').build())
    }

    @Test
    void test_should_be_falsy_when_aggregate_names_are_not_equal() {
        CriteriaFilter filter = new CriteriaFilter(aggregateName: 'Order')

        assert false == filter.test(anEventEnvelope().withAggregateName('Customer').build())
    }

    @Test
    void test_should_be_truthy_when_aggregate_ids_are_equal() {
        CriteriaFilter filter = new CriteriaFilter(aggregateId: AnEventEnvelope.ORDER_ID)

        assert filter.test(anEventEnvelope().withAggregateId(AnEventEnvelope.ORDER_ID).build())
    }

    @Test
    void test_should_be_falsy_when_aggregate_ids_are_not_equal() {
        CriteriaFilter filter = new CriteriaFilter(aggregateId: AnEventEnvelope.ORDER_ID)

        assert false == filter.test(anEventEnvelope().withAggregateId(AnEventEnvelope.ANOTHER_ORDER_ID).build())
    }

}
