package de.oneos.cqrs.readmodels

import org.junit.Test
import org.springframework.jdbc.core.JdbcOperations

import static org.mockito.Mockito.mock
import static org.mockito.Mockito.verify

class SpringJdbcModelsTest {
    JdbcOperations jdbcTemplate = mock(JdbcOperations)

    @Test
    void should_build_create_projection() {
        def eventFilterA = [eventName: 'Device was registered']
        def projectionFunctionA = { models, event ->
            models.add(new SampleReadModel(
                    id: UUID.fromString(event.aggregateId),
                    name: event.attributes.name
            ))
        }

        Projection projection = new Projection(eventFilter: eventFilterA, function: projectionFunctionA)
        SpringJdbcModels springJdbcModels = new SpringJdbcModels(jdbcTemplate: jdbcTemplate)
        def event = [
            aggregateId: UUID.randomUUID().toString(),
            attributes: [
                name: 'SAMPLE READ MODEL NAME'
            ]
        ]
        projection.applyTo(springJdbcModels, event)

        verify(jdbcTemplate).update('INSERT INTO sample_read_model(id, name) VALUES (?, ?);', UUID.fromString(event.aggregateId), event.attributes.name)
    }

    @Test
    void should_build_update_projection() {
        def projections = {
            project(eventName: 'Device was locked out') { models, event ->
                models.findAll { model ->
                    model.id == UUID.fromString(event.aggregateId)
                }.each { model ->
                    model.locked = true
                }
            }
        }
    }

    @Test
    void should_build_delete_projection() {
        def projections = {
            project(eventName: 'Device was deregistered') { models, event ->
                models.findAll { model ->
                    model.id == UUID.fromString(event.aggregateId)
                }.delete()
            }
        }
    }

    static class SampleReadModel {

        UUID id
        String name
    }
}
