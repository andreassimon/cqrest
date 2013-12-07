package de.oneos.projections

class ConnectableObservable<T> extends Observable<T> {

    ConnectableObservable(rx.observables.ConnectableObservable<T> wrappee) {
        super(wrappee)
    }

    def connect() {
        ((rx.observables.ConnectableObservable<T>)wrappee).connect()
    }

}
