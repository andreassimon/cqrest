package de.oneos.projections

import org.junit.*
import static org.mockito.Mockito.*

import de.oneos.readmodels.*
import de.oneos.eventstore.*


class ProjectingEventConsumerTest {
    public static final String APPLICATION = 'APPLICATION NAME'
    public static final String BOUNDED_CONTEXT = 'BOUNDED CONTEXT NAME'
    public static final String AGGREGATE = 'AGGREGATE NAME'
    public static final UUID AGGREGATE_ID = UUID.fromString('1090ce19-e98c-4e34-9878-104aaaabc9ff')
    public static final UUID CORRELATION_ID = UUID.fromString('eb833f0c-f59b-4410-b409-03b991f85a76')
    public static final String USER = 'a.user'

    EventSupplier eventSupplier = mock(EventSupplier)

    ProjectingEventConsumer projectingEventConsumer

    Map<String, ?> criteriaA = [
        applicationName: APPLICATION,
        boundedContextName: BOUNDED_CONTEXT,
        aggregateName: AGGREGATE,
        eventName: 'EVENT A'
    ]

    Map<String, ?> criteriaB = [
        applicationName: APPLICATION,
        boundedContextName: BOUNDED_CONTEXT,
        aggregateName: AGGREGATE,
        eventName: 'EVENT B'
    ]

    Collection<Projection> someProjections = [
        new FunctionalProjection(function: {}, criteria: criteriaA),
        new FunctionalProjection(function: {}, criteria: criteriaB)
    ]

    def projection
    def readModels = new StubbedReadmodels()

    @Before
    void setUp() {
        projection = mock(Projection)
        projectingEventConsumer = new ProjectingEventConsumer()
    }


    @Test
    void should_register_all_projections_at_the_given_EventSupplier() {
        someProjections.each { projectingEventConsumer.add it }

        projectingEventConsumer.subscribeForEventsAt(eventSupplier)

        someProjections.each {
           verify(eventSupplier).subscribeTo(it.criteria, projectingEventConsumer)
        }
    }


    @Test
    void should_pass_the_read_models_to_projections() {
        when(projection.isApplicableTo(anEventEnvelope)).thenReturn(true)
        projectingEventConsumer.readmodels = readModels
        projectingEventConsumer.add projection

        projectingEventConsumer.process(anEventEnvelope)

        projectingEventConsumer.projections.each { projection ->
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
        projectingEventConsumer.add projection

        projectingEventConsumer.process(anEventEnvelope)

        verify(projection).applyTo(org.mockito.Matchers.any(Readmodels), eq(anEventEnvelope))
    }

    @Test
    void should_drop_events_that_do_not_match_any_filter() {
        when(projection.isApplicableTo(anEventEnvelope)).thenReturn(false)
        projectingEventConsumer.add projection

        projectingEventConsumer.process(anEventEnvelope)

        verify(projection, never()).applyTo(org.mockito.Matchers.any(Readmodels), eq(anEventEnvelope))
    }
}
