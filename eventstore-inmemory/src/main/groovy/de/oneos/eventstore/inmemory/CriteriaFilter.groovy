package de.oneos.eventstore.inmemory

import de.oneos.eventsourcing.EventEnvelope


class CriteriaFilter {
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

}
