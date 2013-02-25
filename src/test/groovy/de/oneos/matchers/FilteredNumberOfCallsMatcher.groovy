package de.oneos.matchers

import org.hamcrest.*

import de.oneos.eventstore.*


class FilteredNumberOfCallsMatcher extends TypeSafeMatcher<TestableClosure> {

    Closure<Boolean> callFilter
    int expectedNumberOfCalls


    public FilteredNumberOfCallsMatcher(Closure<Boolean> callFilter, int expectedNumberOfCalls) {
        this.callFilter = callFilter
        this.expectedNumberOfCalls = expectedNumberOfCalls
    }


    @Override
    protected boolean matchesSafely(TestableClosure closure) {
        return actualNumberOfCalls(closure) == expectedNumberOfCalls
    }

    protected actualNumberOfCalls(TestableClosure closure) {
        closure.getNumberOfFilteredCalls(callFilter)
    }

    @Override
    void describeTo(Description description) {
        description.appendText("${explain(expectedNumberOfCalls)}")
    }

    @Override
    protected void describeMismatchSafely(TestableClosure closure, Description mismatchDescription) {
        mismatchDescription.appendText(explain(actualNumberOfCalls(closure)))
    }

    protected explain(int numberOfCalls) {
        if(numberOfCalls == 0) return 'was never called with criteria'
        if(numberOfCalls == 1) return 'was called once with criteria'
        if(numberOfCalls == 2) return 'was called twice with criteria'
        return "was called $numberOfCalls times with criteria"
    }

}
