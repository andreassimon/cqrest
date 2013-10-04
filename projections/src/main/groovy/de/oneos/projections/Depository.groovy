package de.oneos.projections

import org.apache.commons.logging.*

class Depository<T> {
    static Log log = LogFactory.getLog(Depository)


    Map<UUID, Resource<T>> cache = [:]

    void put(Resource<T> r) {
        if(r.aggregateId != null) {
            log.debug "Resource ${r.body.getClass().simpleName}#{$r.aggregateId} is updated"
            cache[r.aggregateId] = r
        }
    }

    Resource<T> get(UUID k) {
        return getAt(k)
    }

    Resource<T> getAt(UUID k) {
        return cache[k]
    }

    Collection<Resource<T>> getAll() {
        return cache.values()
    }
}
