package de.oneos.cqrs.readmodels

class ProjectionBuilder {
    List builtProjections

    List<Projection> buildFrom(Closure closure) {
        builtProjections = []
        closure.delegate = this
        closure.resolveStrategy = Closure.DELEGATE_FIRST
        closure()
        return builtProjections
    }

    void project(Map eventFilter, Closure function) {
        builtProjections << new Projection(eventFilter: eventFilter, function: function)
    }
}
