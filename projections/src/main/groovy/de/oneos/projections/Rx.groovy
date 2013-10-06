package de.oneos.projections

import org.apache.commons.logging.Log


class Rx {
    public static final Closure<Void> logReactiveError(Log log) {
        return { Exception it -> log.error it.message, it }
    }
    public static final Closure<Void> logSequenceFinished(Log log) {
        return { log.debug "Sequence finished" }
    }
}
