package de.oneos.projections

import de.oneos.eventselection.*


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

    void project(Map<String, ?> criteria, Closure function) {
        builtProjections << new FunctionalProjection(criteria: criteria, function: function)
    }
}
