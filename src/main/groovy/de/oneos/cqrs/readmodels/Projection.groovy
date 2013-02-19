package de.oneos.cqrs.readmodels

class Projection {

    EventFilter eventFilter
    Closure function

    @Override
    String toString() {
        "Projection<$eventFilter, ${function.toString()}>"
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
