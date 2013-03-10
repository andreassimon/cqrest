package de.oneos.readmodels

import org.junit.*
import static org.mockito.Mockito.*

import de.oneos.eventselection.*


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

    def projection
    def readModels = new StubbedReadmodels()

    @Before
    void setUp() {
        projection = mock(Projection)
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
        when(projection.isApplicableTo(anEvent)).thenReturn(true)
        projectingEventProcessor.readmodels = readModels
        projectingEventProcessor.add projection

        projectingEventProcessor.process(anEvent)

        projectingEventProcessor.projections.each { projection ->
            verify(projection).applyTo(eq(readModels), anyObject())
        }
    }

    def getAnEvent() { [:] }

    @Test
    void should_forward_events_to_any_matching_projection() {
        when(projection.isApplicableTo(anEvent)).thenReturn(true)
        projectingEventProcessor.add projection

        projectingEventProcessor.process(anEvent)

        verify(projection).applyTo(any(Readmodels), eq(anEvent))
    }

    @Test
    void should_drop_events_that_do_not_match_any_filter() {
        when(projection.isApplicableTo(anEvent)).thenReturn(false)
        projectingEventProcessor.add projection

        projectingEventProcessor.process(anEvent)

        verify(projection, never()).applyTo(any(Readmodels), eq(anEvent))
    }
}
