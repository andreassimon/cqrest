package de.oneos.cqrs.readmodels

interface Models {

    void add(newModelInstance)

    Selection findAll(Closure filter)

    void removeAll(Closure filter)

    void materialize()

}
