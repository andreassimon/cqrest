package org.cqrest.eventstore

import org.cqrest.eventsourcing.EventEnvelope


class CriteriaFilter implements rx.util.functions.Func1<EventEnvelope, Boolean> {
    private final Map<String, ?> criteria

    CriteriaFilter(Map<String, ?> criteria) {
        assert null != criteria

        final Set<String> invalidKeys = criteria.keySet().minus(['aggregateName', 'aggregateId', 'eventName'])
        if(!invalidKeys.empty) {
            throw new IllegalArgumentException("criteria must not contain $invalidKeys")
        }
        this.criteria = criteria.asImmutable()
    }

    Closure<Boolean> test = { EventEnvelope candidate ->
        criteria.every { attribute, value ->
            if(value instanceof Collection) {
                return value.contains(candidate[attribute])
            }
            value == candidate[attribute]
        }
    }

    @Override
    Boolean call(EventEnvelope candidate) {
        return this.test.call(candidate)
    }

}
