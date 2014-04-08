package de.oneos.eventstore.springjdbc

import org.apache.commons.logging.Log
import org.apache.commons.logging.LogFactory
import org.springframework.dao.DataAccessException

import rx.Subscription

import de.oneos.eventsourcing.EventEnvelope


class QueryOnSubscribe implements rx.Observable.OnSubscribeFunc<EventEnvelope> {

    public static Log log = LogFactory.getLog(QueryOnSubscribe)

    final Map<String, ?> criteria
    final SpringJdbcEventStore store


    QueryOnSubscribe(Map<String, ?> criteria, SpringJdbcEventStore store) {
        this.store = store
        this.criteria = criteria
    }

    @Override
    // TODO Remove Spring exception from signature
    Subscription onSubscribe(rx.Observer<? super EventEnvelope> observer) throws DataAccessException {
        // TODO Fork a thread ?
        store.queryByCriteria(criteria, new ReactiveRowCallbackHandler(observer))
        // TODO test
        observer.onCompleted()
        return new Subscription() {
            @Override
            void unsubscribe() {
                log.info("Tried to unsubscribe from SpringJdbcEventStore query; that's not implemented by now.")
            }
        }
    }

}
