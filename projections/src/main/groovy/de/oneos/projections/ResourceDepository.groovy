package de.oneos.projections

import org.apache.commons.logging.*

class ResourceDepository<T> {
    public static final String UNDEFINED = null
    public static Log log = LogFactory.getLog(this)


    final Map<UUID, Resource<T>> cache = [:]
    protected String name


    @Deprecated
    ResourceDepository() { this(UNDEFINED) }

    ResourceDepository(String name) {
        this.name = name
    }

    void put(Resource<T> r) {
        if(r.aggregateId != null) {
            log.debug "Resource ${name ?: r.body.getClass().simpleName}#{$r.aggregateId} is updated"
            cache[r.aggregateId] = r
        }
    }

    boolean contains(UUID k) {
        return cache.containsKey(k)
    }

    Resource<T> get(UUID k) {
        return getAt(k)
    }

    Resource<T> getAt(UUID k) {
        return cache[k]
    }

    Collection<T> getAllBodies() {
        return getAll()*.body
    }

    Collection<Resource<T>> getAll() {
        return cache.values()
    }

    T getBody(UUID k) {
        return get(k)?.body
    }

}
