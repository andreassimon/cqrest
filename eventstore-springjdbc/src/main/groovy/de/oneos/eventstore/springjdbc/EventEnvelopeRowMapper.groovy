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
          rs.getString('application_name'),
          rs.getString('bounded_context_name'),
          rs.getString('aggregate_name'),
          (UUID) rs.getObject('aggregate_id'),
          rs.getString('event_name'),
          json.parseText(rs.getString('attributes')) as Map,
          rs.getInt('sequence_number'),
          rs.getTimestamp('timestamp'),
          (UUID) rs.getObject('correlation_id'),
          rs.getString('user')
        )
    }

}
