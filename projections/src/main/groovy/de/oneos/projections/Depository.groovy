package de.oneos.projections

import org.apache.commons.logging.*


class Depository<T> {
    static Log log = LogFactory.getLog(Depository)


    Collection<T> cache = new ArrayList<>()

    void put(T t) {
        log.debug "Added $t"
        cache << t
    }

    def Collection<T> getAll() {
        return Collections.unmodifiableCollection(cache)
    }
}
