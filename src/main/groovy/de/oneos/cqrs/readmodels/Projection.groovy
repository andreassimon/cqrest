package de.oneos.cqrs.readmodels

public interface Projection {

    Models applyTo(Models models, deserializedEvent)

}
