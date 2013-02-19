package de.oneos.cqrs.readmodels

import org.junit.Test

import static org.mockito.Mockito.*
import org.junit.Ignore
import org.junit.Before


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

    Collection<Projection> someProjections = [
        new FunctionalProjection(function: {}, eventFilter: eventFilterA),
        new FunctionalProjection(function: {}, eventFilter: eventFilterB)
    ]

    def readModels = new StubbedModels()

    @Before
    void setUp() {
        projectingEventProcessor = new ProjectingEventProcessor()
    }


    @Test
    void should_register_all_projections_at_the_given_EventSupplier() {
        someProjections.each { projectingEventProcessor.add it }

        projectingEventProcessor.subscribeForEventsAt(eventSupplier)

        someProjections.each {
           verify(eventSupplier).subscribeTo(it.eventFilter, projectingEventProcessor)
        }
    }


    @Test
    void should_pass_the_read_models_to_projections() {
        projectingEventProcessor.readModels = readModels
        projectingEventProcessor.add mock(Projection)

        projectingEventProcessor.process(anEvent)

        projectingEventProcessor.projections.each { projection ->
            verify(projection).applyTo(eq(readModels), anyObject())
        }
    }

    def getAnEvent() {
        []
    }

    @Ignore("Must be implemented!") // depends on EventFilterTest
    @Test
    void should_drop_events_that_do_not_match_any_filter() {}

    @Ignore("Must be implemented!") // depends on EventFilterTest
    @Test
    void should_forward_events_to_the_matching_projection() {}
}
