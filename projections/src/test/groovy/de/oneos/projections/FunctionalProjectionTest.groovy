package de.oneos.projections;

import org.junit.Test
import static org.junit.Assert.*
import static org.hamcrest.Matchers.*


public class FunctionalProjectionTest {

    def anyEvent = new Object() {
        String toString() { 'AN EVENT' }
        boolean equals(Object that) { this.toString() == that.toString() }
    }

    FunctionalProjection functionalProjection


    @Test
    void is_applicable_to_any_event_when_criteria_are_unconstrained() {
        functionalProjection = new FunctionalProjection(criteria: [:])

        assertThat functionalProjection.isApplicableTo(anyEvent), equalTo(true)
    }

    @Test
    void is_not_applicable_to_events_without_matching_event_names() {
        functionalProjection = new FunctionalProjection(criteria: [eventName: "Order line was added"])

        assertThat functionalProjection.isApplicableTo([eventName: "Order line was removed"]), equalTo(false)
    }

    @Test
    void is_applicable_to_events_with_matching_event_names() {
        functionalProjection = new FunctionalProjection(criteria: [eventName: "Order line was added"])

        assertThat functionalProjection.isApplicableTo([eventName: "Order line was added"]), equalTo(true)
    }

    @Test
    void is_not_applicable_to_events_without_event_names_matching_one_of_several_event_names() {
        functionalProjection = new FunctionalProjection(criteria: [eventName: ["Order line was added", "Order line was removed"] ])

        assertThat functionalProjection.isApplicableTo([eventName: "Order was cancelled"]), equalTo(false)
    }

    @Test
    void is_applicable_to_events_with_event_names_matching_one_of_several_event_names() {
        functionalProjection = new FunctionalProjection(criteria: [eventName: ["Order line was added", "Order line was removed"] ])

        assertThat functionalProjection.isApplicableTo([eventName: "Order line was added"]), equalTo(true)
    }

}
