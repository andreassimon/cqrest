package de.oneos.cqrs.readmodels;

import org.junit.Test

import static org.mockito.Mockito.mock
import static org.mockito.Mockito.verify;

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
