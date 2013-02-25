package de.oneos

import org.hamcrest.Matcher
import de.oneos.eventstore.TestableClosure

import de.oneos.matchers.*


class Matchers {

    static Matcher<TestableClosure> wasCalledOnce() {
        return new ClosureWasCalledOnceMatcher()
    }

    static Matcher<TestableClosure> wasCalledOnceWith(Closure<Boolean> filter) {
        return new FilteredNumberOfCallsMatcher(filter, 1)
    }

}
