package de.oneos.cqrs.readmodels

import org.junit.Test

import static org.hamcrest.Matchers.equalTo
import static org.junit.Assert.assertThat

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

        List<Projection> actualProjections = projectionBuilder.buildFrom projections

        assertThat actualProjections, equalTo([
            new Projection(eventFilter: eventFilterA, function: projectionFunctionA),
            new Projection(eventFilter: eventFilterB, function: projectionFunctionB)
        ])
    }


    static class SampleReadModel {

        UUID deviceId
        String deviceName
    }
}
