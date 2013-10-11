package de.oneos.projections

import org.apache.commons.logging.Log

import rx.lang.groovy.*
import rx.util.functions.*


class Rx {

    public static final Action1<Throwable> logReactiveError(Log log) {
        return new GroovyActionWrapper<>({ Exception it -> log.error it.message, it })
    }

    public static final Action0 logSequenceFinished(Log log) {
        return new GroovyActionWrapper<>({ log.debug "Sequence finished" })
    }

}
