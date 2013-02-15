package de.oneos.cqrs.readmodels

class ProjectionBuilder {
    List builtProjections

    private ProjectionBuilder() {
        builtProjections = []
    }


    static List<Projection> buildFrom(Closure closure) {
        ProjectionBuilder instance = new ProjectionBuilder()
        closure.delegate = instance
        closure.resolveStrategy = Closure.DELEGATE_FIRST
        closure()
        return instance.builtProjections
    }

    void project(Map eventFilter, Closure function) {
        builtProjections << new Projection(eventFilter: eventFilter, function: function)
    }
}
