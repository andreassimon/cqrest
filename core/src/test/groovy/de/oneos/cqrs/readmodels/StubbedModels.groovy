package de.oneos.cqrs.readmodels

class StubbedModels implements Models {

    @Override
    void add(Object newModelInstance) { }

    @Override
    Selection findAll(Closure filter) { }

    @Override
    void removeAll(Closure filter) { }

    @Override
    void materialize() { }

    @Override
    String toString() {
        'STUBBED_MODELS'
    }

    @Override
    boolean equals(Object that) {
        this.toString() == that.toString()
    }
}
