package org.cqrest.test

import junit.framework.AssertionFailedError


class Expect {

    static void expect(Class<? extends Throwable> exceptionClass, Closure<?> block) {
        try {
            block.call()
            throw new AssertionFailedError("Expected $exceptionClass, but none was thrown")
        } catch (exception) {
            if(exception.class != exceptionClass) {
                throw exception
            }
        }
    }

}
