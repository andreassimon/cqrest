package de.oneos.cqrs.readmodels

import org.junit.Test

import static org.hamcrest.Matchers.equalTo
import static org.junit.Assert.assertThat
import org.junit.Ignore

class ProjectionBuilderTest {

    ProjectionBuilder projectionBuilder = new ProjectionBuilder()

    def eventFilterA = [eventName: 'Device was registered']
    def projectionFunctionA = { models, event ->
        models.add(new DeviceDetails(
                deviceId: UUID.fromString(event.aggregateId),
                deviceName: event.attributes.deviceName
        ))
    }

    def eventFilterB = [eventName: 'Device was locked out']
    def projectionFunctionB = { models, event ->
        models.findAll { model ->
            model.deviceId == UUID.fromString(event.aggregateId)
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

        assertThat actualProjections[0].eventFilter, equalTo(eventFilterA)
        assertThat actualProjections[0].function, equalTo(projectionFunctionA)

        assertThat actualProjections[1].eventFilter, equalTo(eventFilterB)
        assertThat actualProjections[1].function, equalTo(projectionFunctionB)
    }

    @Ignore
    @Test
    void should_build_update_projection() {
        def projections = {
            project(eventName: 'Device was locked out') { models, event ->
                models.findAll { model ->
                    model.deviceId == UUID.fromString(event.aggregateId)
                }.each { model ->
                    model.locked = true
                }
            }
        }
    }

    @Ignore
    @Test
    void should_build_delete_projection() {
        def projections = {
            project(eventName: 'Device was deregistered') { models, event ->
                models.findAll { model ->
                    model.deviceId == UUID.fromString(event.aggregateId)
                }.delete()
            }
        }
    }

    static class DeviceDetails {

        UUID deviceId
        String deviceName
    }
}
