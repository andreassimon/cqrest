package de.oneos.projections

import de.oneos.eventselection.*
import de.oneos.readmodels.*


class FunctionalProjection implements Projection {

    Map<String, ?> criteria
    Closure function

    @Override
    String toString() {
        "FunctionalProjection<$eventFilter, ${function.toString()}>"
    }

    @Override
    boolean equals(Object that) {
        null != that &&
        this.getClass() == that.getClass() &&
        this.criteria == that.criteria &&
        this.function == that.function
    }

    Boolean isApplicableTo(event) {
        if(criteria.eventName) {
            switch(criteria.eventName) {
                case String:
                    return criteria.eventName == event.eventName
                case Collection:
                    return criteria.eventName.any { it == event.eventName }
                default:
                    return false
            }
        }
        return true
    }

    Readmodels applyTo(Readmodels models, event) {
        function(models, event)
        models.materialize()
    }
}
