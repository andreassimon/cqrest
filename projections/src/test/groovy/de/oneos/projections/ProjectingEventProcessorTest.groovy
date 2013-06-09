package de.oneos.projections

import org.junit.*
import static org.mockito.Mockito.*

import de.oneos.eventselection.*
import de.oneos.readmodels.*
import de.oneos.eventstore.EventEnvelope


class ProjectingEventProcessorTest {
    public static final String APPLICATION = 'APPLICATION NAME'
    public static final String BOUNDED_CONTEXT = 'BOUNDED CONTEXT NAME'
    public static final String AGGREGATE = 'AGGREGATE NAME'
    public static final UUID AGGREGATE_ID = UUID.fromString('1090ce19-e98c-4e34-9878-104aaaabc9ff')
    public static final UUID CORRELATION_ID = UUID.fromString('eb833f0c-f59b-4410-b409-03b991f85a76')
    public static final String USER = 'a.user'

    EventSupplier eventSupplier = mock(EventSupplier)

    ProjectingEventProcessor projectingEventProcessor

    EventFilter eventFilterA = new MapEventFilter(
        applicationName: APPLICATION,
        boundedContextName: BOUNDED_CONTEXT,
        aggregateName: AGGREGATE,
        eventName: 'EVENT A'
    )

    EventFilter eventFilterB = new MapEventFilter(
        applicationName: APPLICATION,
        boundedContextName: BOUNDED_CONTEXT,
        aggregateName: AGGREGATE,
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
        when(projection.isApplicableTo(anEventEnvelope)).thenReturn(true)
        projectingEventProcessor.readmodels = readModels
        projectingEventProcessor.add projection

        projectingEventProcessor.publish(anEventEnvelope)

        projectingEventProcessor.projections.each { projection ->
            verify(projection).applyTo(eq(readModels), anyObject())
        }
    }

    def getAnEventEnvelope() {
        new EventEnvelope(
            APPLICATION,
            BOUNDED_CONTEXT,
            AGGREGATE,
            AGGREGATE_ID,
            [:],
            0,
            new Date(2013-1900, 5, 9, 19, 18, 00),
            CORRELATION_ID,
            USER
        )
    }

    @Test
    void should_forward_events_to_any_matching_projection() {
        when(projection.isApplicableTo(anEventEnvelope)).thenReturn(true)
        projectingEventProcessor.add projection

        projectingEventProcessor.publish(anEventEnvelope)

        verify(projection).applyTo(org.mockito.Matchers.any(Readmodels), eq(anEventEnvelope))
    }

    @Test
    void should_drop_events_that_do_not_match_any_filter() {
        when(projection.isApplicableTo(anEventEnvelope)).thenReturn(false)
        projectingEventProcessor.add projection

        projectingEventProcessor.publish(anEventEnvelope)

        verify(projection, never()).applyTo(org.mockito.Matchers.any(Readmodels), eq(anEventEnvelope))
    }
}
