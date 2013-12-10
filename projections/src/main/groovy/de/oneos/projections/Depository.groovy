package de.oneos.projections

import org.apache.commons.logging.*


class Depository<T> {
    public static Log log = LogFactory.getLog(this)


    Collection<T> cache = new HashSet<>()

    void put(T t) {
        log.debug "Added $t"
        cache << t
    }

    def Collection<T> getAll() {
        return Collections.unmodifiableCollection(cache)
    }
}
