package de.oneos.cqrs.readmodels

public interface Selection {

    void delete()

    void each(Closure update)

}
