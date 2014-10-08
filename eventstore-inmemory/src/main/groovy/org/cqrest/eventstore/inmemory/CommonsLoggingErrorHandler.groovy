package org.cqrest.eventstore.inmemory

import org.apache.commons.logging.Log
import org.apache.commons.logging.LogFactory

import rx.plugins.RxJavaErrorHandler


class CommonsLoggingErrorHandler extends RxJavaErrorHandler {
    public static Log log = LogFactory.getLog(CommonsLoggingErrorHandler)

    @Override
    void handleError(Throwable e) {
        super.handleError(e)
        log.error("${e.getClass().getCanonicalName()} thrown in RxJava", e)
    }

}
