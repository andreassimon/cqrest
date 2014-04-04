package de.oneos.eventstore.springjdbc

import java.sql.ResultSet
import java.sql.SQLException

import groovy.json.JsonSlurper
import org.springframework.jdbc.core.RowMapper

import de.oneos.eventsourcing.EventEnvelope


class EventEnvelopeRowMapper implements RowMapper<EventEnvelope> {

    JsonSlurper json = new JsonSlurper()


    @Override
    EventEnvelope mapRow(ResultSet rs, int rowNum) throws SQLException {
        new EventEnvelope(
          rs.getString(Schema.APPLICATION_NAME),
          rs.getString(Schema.BOUNDED_CONTEXT_NAME),
          rs.getString(Schema.AGGREGATE_NAME),
          (UUID) rs.getObject(Schema.AGGREGATE_ID),
          rs.getString(Schema.EVENT_NAME),
          json.parseText(rs.getString(Schema.ATTRIBUTES)) as Map,
          rs.getInt(Schema.SEQUENCE_NUMBER),
          rs.getTimestamp(Schema.TIMESTAMP),
          (UUID) rs.getObject(Schema.CORRELATION_ID),
          rs.getString(Schema.USER)
        )
    }

}
