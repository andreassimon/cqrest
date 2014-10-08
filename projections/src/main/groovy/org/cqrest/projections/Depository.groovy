package org.cqrest.projections

import org.apache.commons.logging.*


class Depository<T> implements org.cqrest.reactive.Observer<T> {
    public static Log log = LogFactory.getLog(this)


    Collection<T> cache = new HashSet<>()

    void put(T t) {
        log.debug "Added $t"
        cache << t
    }

    def Collection<T> getAll() {
        return Collections.unmodifiableCollection(cache)
    }

    @Override
    void onCompleted() {
        log.info "Event sequence on $this is completed"
    }

    @Override
    void onError(Throwable e) {
        log.error "${e.getClass().getCanonicalName()}: $e.message", e
    }

    @Override
    void onNext(T item) {
        put(item)
    }

}
