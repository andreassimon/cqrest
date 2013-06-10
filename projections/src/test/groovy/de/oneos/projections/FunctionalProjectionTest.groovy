package de.oneos.projections;

import org.junit.Test
import static org.mockito.Mockito.*

public class FunctionalProjectionTest {

    def anEvent = new Object() {
        String toString() { 'AN EVENT' }
        boolean equals(Object that) { this.toString() == that.toString() }
    }

    Map<String, ?> criteria = [:]

    FunctionalProjection functionalProjection


    @Test
    void isApplicableTo_utilizes_its_eventFilter_for_matching() {
        functionalProjection = new FunctionalProjection(criteria: criteria)

        functionalProjection.isApplicableTo(anEvent)

        verify(criteria).matches(anEvent)
    }

}
