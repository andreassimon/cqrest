package org.cqrest.eventstore

class TestableClosure<V> extends Closure<V> {

    List callParameters = []

    TestableClosure(Object owner) {
        super(owner)
    }

    int getNumberOfCalls() { callParameters.size() }

    int getNumberOfFilteredCalls(Closure<Boolean> callFilter) {
        callParameters.findAll({ Object... args ->
            try {
                callFilter(*args)
            } catch (MissingMethodException e) {
                return false
            }
        }).size()
    }

    public Object doCall(Object... args) {
        return call(args);
    }

    @Override
    V call(Object... args) {
        callParameters << args
        return null
    }

}
