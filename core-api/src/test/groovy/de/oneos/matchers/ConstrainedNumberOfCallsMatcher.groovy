package de.oneos.matchers

import org.hamcrest.*

import de.oneos.eventstore.*


class ConstrainedNumberOfCallsMatcher extends TypeSafeMatcher<TestableClosure> {

    Class expectedArgumentClass
    Map<String, Object> propertyConstraints
    int expectedNumberOfCalls


    public ConstrainedNumberOfCallsMatcher(Class expectedArgumentClass, Map<String, Object> propertyConstraints, int expectedNumberOfCalls) {
        this.expectedArgumentClass = expectedArgumentClass
        this.propertyConstraints = propertyConstraints
        this.expectedNumberOfCalls = expectedNumberOfCalls
    }


    @Override
    protected boolean matchesSafely(TestableClosure closure) {
        return actualNumberOfCalls(closure) == expectedNumberOfCalls
    }

    protected actualNumberOfCalls(TestableClosure closure) {
        closure.getNumberOfFilteredCalls() { arg ->
            expectedArgumentClass.isAssignableFrom(arg.class) &&
                compliesWith(arg, propertyConstraints)
        }
    }

    protected compliesWith(arg, Map<String, Object> constraints) {
        constraints.collect { property, expectedValue ->
            arg[property] == expectedValue
        }.inject {a, b -> a && b}
    }

    @Override
    void describeTo(Description description) {
        if(expectedNumberOfCalls == 0) description.appendText("*never* to be called with $constraints")
        if(expectedNumberOfCalls == 1) description.appendText("to be called *once* with $constraints")
        if(expectedNumberOfCalls == 2) description.appendText("to be called *twice* with $constraints")
        if(expectedNumberOfCalls > 2) description.appendText("to be called *${expectedNumberOfCalls} times* with $constraints")
    }

    protected getConstraints() {
        "${expectedArgumentClass.canonicalName}$propertyConstraints"
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
        if(numberOfCalls == 0) return "was *never* called with these constraints"
        if(numberOfCalls == 1) return "to be called *once* with these constraints"
        if(numberOfCalls == 2) return "was called *twice* with these constraints"
        return "was called *${expectedNumberOfCalls} times* with these constraints"
    }

}
