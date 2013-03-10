package de.oneos.projections

import de.oneos.readmodels.*


public interface Projection {

    Boolean isApplicableTo(deserializedEvent)
    Readmodels applyTo(Readmodels models, deserializedEvent)

}
