package de.oneos.cqrs.readmodels

import org.junit.Test
import org.springframework.jdbc.core.JdbcOperations

import static org.mockito.Mockito.mock
import static org.mockito.Mockito.verify
import org.junit.Before

class SpringJdbcModelsTest {
    JdbcOperations jdbcTemplate = mock(JdbcOperations)

    SpringJdbcModels springJdbcModels

    @Before
    void setUp() {
        springJdbcModels = new SpringJdbcModels(jdbcTemplate: jdbcTemplate)
    }


    @Test
    void should_build_create_projection() {
        def event = [
            aggregateId: UUID.randomUUID().toString(),
            attributes: [
                camelCaseProperty: 'SAMPLE READ MODEL NAME'
            ]
        ]

        springJdbcModels.add(new SampleReadModel(
            id: UUID.fromString(event.aggregateId),
            camelCaseProperty: event.attributes.camelCaseProperty
        ))
        springJdbcModels.materialize()

        verify(jdbcTemplate).update('INSERT INTO sample_read_model(id, camel_case_property) VALUES (?, ?);', UUID.fromString(event.aggregateId), event.attributes.camelCaseProperty)
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
        String camelCaseProperty
    }
}
