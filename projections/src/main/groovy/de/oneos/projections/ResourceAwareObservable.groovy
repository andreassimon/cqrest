package de.oneos.projections


class ResourceAwareObservable<T> {

    @Delegate
    de.oneos.projections.Observable<T> wrappee

    ResourceAwareObservable(de.oneos.projections.Observable<T> wrappee) {
        this.wrappee = wrappee
    }

}
