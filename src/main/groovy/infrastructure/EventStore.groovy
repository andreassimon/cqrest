package infrastructure

import org.springframework.jdbc.core.JdbcTemplate

import static infrastructure.utilities.GenericEventSerializer.toJSON

class EventStore {
    JdbcTemplate jdbcTemplate

    def save(event) {
        jdbcTemplate.update(
            "INSERT INTO Events (AggregateClassName, AggregateId, EventName, Attributes, Timestamp) VALUES (?,?,?,?,?);",
                event.aggregateClassName,
                event.aggregateId,
                event.name,
                toJSON(event),
                event.timestamp)
    }
}
