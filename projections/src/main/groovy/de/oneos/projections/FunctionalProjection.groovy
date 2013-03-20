package de.oneos.projections

import de.oneos.eventselection.*
import de.oneos.readmodels.*


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

    Boolean isApplicableTo(event) {
        eventFilter.matches(event)
    }

    Readmodels applyTo(Readmodels models, event) {
        function(models, event)
        models.materialize()
    }
}