package de.oneos

import org.hamcrest.Matcher
import de.oneos.eventstore.TestableClosure

import de.oneos.matchers.ClosureWasCalledOnceMatcher

class Matchers {

    static Matcher<TestableClosure> wasCalledOnce() {
        return new ClosureWasCalledOnceMatcher()
    }

}
