package de.oneos.matchers

import org.hamcrest.*

import de.oneos.eventstore.*


class ConstrainedNumberOfCallsMatcher extends TypeSafeMatcher<TestableClosure> {

    Map<String, Object> propertyConstraints
    int expectedNumberOfCalls


    public ConstrainedNumberOfCallsMatcher(Map<String, Object> propertyConstraints, int expectedNumberOfCalls) {
        this.propertyConstraints = propertyConstraints
        this.expectedNumberOfCalls = expectedNumberOfCalls
    }


    @Override
    protected boolean matchesSafely(TestableClosure closure) {
        return actualNumberOfCalls(closure) == expectedNumberOfCalls
    }

    protected actualNumberOfCalls(TestableClosure closure) {
        closure.getNumberOfFilteredCalls() { arg ->
            propertyConstraints.collect { property, expectedValue ->
                arg[property] == expectedValue
            }.inject {a, b -> a && b}
        }
    }

    @Override
    void describeTo(Description description) {
        if(expectedNumberOfCalls == 0) description.appendText("*never* to be called with $propertyConstraints")
        if(expectedNumberOfCalls == 1) description.appendText("to be called *once* with $propertyConstraints")
        if(expectedNumberOfCalls == 2) description.appendText("to be called *twice* with $propertyConstraints")
        if(expectedNumberOfCalls > 2) description.appendText("to be called *${expectedNumberOfCalls} times* with $propertyConstraint")
    }

    @Override
    protected void describeMismatchSafely(TestableClosure closure, Description mismatchDescription) {
        mismatchDescription.appendText(explain(actualNumberOfCalls(closure)))
        mismatchDescription.appendText('\nWas instead called with:\n')
        closure.getCallParameters().each { call ->
            mismatchDescription.appendText('  ')
            mismatchDescription.appendValue(call)
            mismatchDescription.appendText('\n')
        }
    }

    protected explain(int numberOfCalls) {
        if(numberOfCalls == 0) return "was *never* called with $propertyConstraints"
        if(numberOfCalls == 1) return "to be called *once* with $propertyConstraints"
        if(numberOfCalls == 2) return "was called *twice* with $propertyConstraints"
        return "was called *${expectedNumberOfCalls} times* with $propertyConstraint"
    }

}
