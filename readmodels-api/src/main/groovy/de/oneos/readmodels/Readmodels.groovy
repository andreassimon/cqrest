package de.oneos.readmodels

@Deprecated
interface Readmodels {

    void add(newModelInstance)

    Selection findAll(Closure filter)

    void removeAll(Closure filter)

    void materialize()

}
