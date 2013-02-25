package de.oneos.eventstore

class TestableClosure<V> extends Closure<V> {

    List callParameters = []

    TestableClosure(Object owner) {
        super(owner)
    }

    int getNumberOfCalls() { callParameters.size() }

    public Object doCall(Object... args) {
        return call(args);
    }

    @Override
    V call(Object... args) {
        callParameters << args
        return null
    }

}
