package de.oneos.readmodels

interface Readmodels {

    void add(newModelInstance)

    Selection findAll(Closure filter)

    void removeAll(Closure filter)

    void materialize()

}
