package de.oneos.projections;

import org.junit.Test
import static org.mockito.Mockito.*

import de.oneos.eventselection.*


public class FunctionalProjectionTest {

    def anEvent = new Object() {
        String toString() { 'AN EVENT' }
        boolean equals(Object that) { this.toString() == that.toString() }
    }

    EventFilter eventFilter = mock(EventFilter)

    FunctionalProjection functionalProjection


    @Test
    void isApplicableTo_utilizes_its_eventFilter_for_matching() {
        functionalProjection = new FunctionalProjection(eventFilter: eventFilter)

        functionalProjection.isApplicableTo(anEvent)

        verify(eventFilter).matches(anEvent)
    }

}
