package de.oneos.eventstore.springjdbc

import java.sql.ResultSet
import java.sql.SQLException

import org.springframework.jdbc.core.RowCallbackHandler

import de.oneos.eventsourcing.EventEnvelope


class ReactiveRowCallbackHandler implements RowCallbackHandler {

    final EventEnvelopeRowMapper rowMapper = new EventEnvelopeRowMapper()
    final rx.Observer<? super EventEnvelope> observer


    ReactiveRowCallbackHandler(rx.Observer<? super EventEnvelope> observer) {
        this.observer = observer
    }


    @Override
    void processRow(ResultSet rs) throws SQLException {
        // TODO handle errors
        observer.onNext(rowMapper.mapRow(rs, rs.row))
    }

}
