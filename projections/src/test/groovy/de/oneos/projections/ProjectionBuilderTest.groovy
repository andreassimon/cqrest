package de.oneos.projections

import org.junit.*
import static org.junit.Assert.*
import static org.hamcrest.Matchers.*

import de.oneos.eventselection.*


class ProjectionBuilderTest {

    ProjectionBuilder projectionBuilder = new ProjectionBuilder()

    def eventFilterA = [eventName: 'Device was registered']
    def projectionFunctionA = { models, event ->
        models.add(new SampleReadModel(
                deviceId: UUID.fromString(event.aggregateId),
                deviceName: event.attributes.name
        ))
    }

    def eventFilterB = [eventName: 'Device was locked out']
    def projectionFunctionB = { models, event ->
        models.findAll { model ->
            model.Id == UUID.fromString(event.aggregateId)
        }.each { model ->
            model.locked = true
        }
    }

    @Test
    void should_build_projection_objects() {
        def projections = {
            project(eventFilterA, projectionFunctionA)
            project(eventFilterB, projectionFunctionB)
        }

        List<Projection> actualProjections = ProjectionBuilder.buildFrom projections

        assertThat actualProjections, equalTo([
            new FunctionalProjection(eventFilter: new MapEventFilter(eventFilterA), function: projectionFunctionA),
            new FunctionalProjection(eventFilter: new MapEventFilter(eventFilterB), function: projectionFunctionB)
        ])
    }


    static class SampleReadModel {

        UUID deviceId
        String deviceName
    }
}
