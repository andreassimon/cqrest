package de.oneos.cqrs.readmodels

class FunctionalProjection implements Projection {

    EventFilter eventFilter
    Closure function

    @Override
    String toString() {
        "FunctionalProjection<$eventFilter, ${function.toString()}>"
    }

    @Override
    boolean equals(Object that) {
        this.eventFilter == that.eventFilter &&
        this.function == that.function
    }

    Models applyTo(Models models, deserializedEvent) {
        function(models, deserializedEvent)
        models.materialize()
    }
}
