package de.oneos.cqrs.readmodels

import org.junit.Test

import static org.mockito.Mockito.*
import org.junit.Ignore


class ProjectingEventProcessorTest {
    EventSupplier eventSupplier = mock(EventSupplier)

    ProjectingEventProcessor projectingEventProcessor

    EventFilter eventFilterA = new MapEventFilter(
        applicationName: 'APPLICATION NAME',
        boundedContextName: 'BOUNDED CONTEXT NAME',
        aggregateName: 'AGGREGATE NAME',
        eventName: 'EVENT A'
    )

    EventFilter eventFilterB = new MapEventFilter(
        applicationName: 'APPLICATION NAME',
        boundedContextName: 'BOUNDED CONTEXT NAME',
        aggregateName: 'AGGREGATE NAME',
        eventName: 'EVENT B'
    )

    @Test
    void should_register_all_projections_at_the_given_EventSupplier() {
        projectingEventProcessor = new ProjectingEventProcessor()
        projectingEventProcessor.add projectionWith(eventFilter: eventFilterA)
        projectingEventProcessor.add projectionWith(eventFilter: eventFilterB)

        projectingEventProcessor.subscribeForEventsAt(eventSupplier)

        verify(eventSupplier).subscribeTo(eventFilterA, projectingEventProcessor)
        verify(eventSupplier).subscribeTo(eventFilterB, projectingEventProcessor)
    }

    private projectionWith(Map config) {
        new Projection([function: {}] + config)
    }

    @Ignore("Must be implemented!") // depends on EventFilterTest
    @Test
    void should_drop_events_that_do_not_match_any_filter() {}

    @Ignore("Must be implemented!") // depends on EventFilterTest
    @Test
    void should_forward_events_to_the_matching_projection() {}
}
