package de.oneos

import org.hamcrest.Matcher
import de.oneos.eventstore.TestableClosure

import de.oneos.matchers.*


class Matchers {

    static Matcher<TestableClosure> wasCalledOnce() {
        return new ClosureWasCalledOnceMatcher()
    }

    static Matcher<TestableClosure> wasCalledOnceWith(Class argumentClass = Object, Map<String, Object> propertyConstraints) {
        return new ConstrainedNumberOfCallsMatcher(argumentClass, propertyConstraints, 1)
    }

    static Matcher<TestableClosure> wasCalledTwiceWith(Class argumentClass = Object, Map<String, Object> propertyConstraints) {
        return new ConstrainedNumberOfCallsMatcher(argumentClass, propertyConstraints, 2)
    }

    static Matcher<TestableClosure> wasCalledOnceWith(Closure<Boolean> filter) {
        return new FilteredNumberOfCallsMatcher(filter, 1)
    }

    static Matcher<TestableClosure> wasCalledTwiceWith(Closure<Boolean> filter) {
        return new FilteredNumberOfCallsMatcher(filter, 2)
    }

}
