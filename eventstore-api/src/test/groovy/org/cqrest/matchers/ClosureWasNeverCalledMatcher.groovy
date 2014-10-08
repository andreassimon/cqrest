package org.cqrest.matchers

import org.cqrest.eventstore.*
import org.hamcrest.*

class ClosureWasNeverCalledMatcher extends TypeSafeMatcher<TestableClosure> {

    @Override
    protected boolean matchesSafely(TestableClosure closure) {
        return closure.getNumberOfCalls() == 0
    }

    @Override
    void describeTo(Description description) {
        description.appendText(explain(0))
    }

    @Override
    protected void describeMismatchSafely(TestableClosure closure, Description mismatchDescription) {
        mismatchDescription.appendText(explain(closure.getNumberOfCalls()))
    }

    protected explain(int numberOfCalls) {
        if(numberOfCalls == 0) return 'was never called'
        if(numberOfCalls == 1) return 'was called once'
        if(numberOfCalls == 2) return 'was called twice'
        return "was called $numberOfCalls times"
    }

}
