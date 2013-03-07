package de.oneos.cqrs.readmodels

public interface Projection {

    Boolean isApplicableTo(deserializedEvent)
    Models applyTo(Models models, deserializedEvent)

}
