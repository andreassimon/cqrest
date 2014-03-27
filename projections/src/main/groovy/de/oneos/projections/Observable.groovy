package de.oneos.projections

import rx.lang.groovy.*
import rx.observables.*
import rx.util.functions.*

/**
 *  Wrapper for RxJava's Observable
 */
class Observable<T> {

    rx.Observable<T> wrappee

    Observable(rx.Observable<T> wrappee) {
        this.wrappee = wrappee
    }

    Observable<GroupedObservable> groupBy(Closure keySelector) {
        return new Observable<GroupedObservable>(wrappee.groupBy(new GroovyFunctionWrapper<>(keySelector)))
    }

    public <R> Observable<R> map(Closure<R> func) {
        return new Observable<R>(wrappee.map(new GroovyFunctionWrapper<>(func)))
    }

    Observable<T> filter(Closure predicate) {
        return new Observable<T>(wrappee.filter(new GroovyFunctionWrapper<>(predicate)))
    }

    public <R> Observable<R> flatMap(Closure<R> func) {
        return new Observable<R>(wrappee.flatMap(new GroovyFunctionWrapper(func)))
    }

    def subscribe(Observer observer) {
        return wrappee.subscribe(new ObserverAdapter(observer))
    }

    def subscribe(Closure onNext, Action1 onError, Action0 onComplete) {
        return wrappee.subscribe(new GroovyActionWrapper<>(onNext), onError, onComplete)
    }

    // TODO delete
    def deposit(Depository<T> depository) {
        return subscribe(depository.&put, Rx.logReactiveError(depository.log), Rx.logSequenceFinished(depository.log))
    }

    ConnectableObservable<T> publish() {
        return new ConnectableObservable<T>(wrappee.publish())
    }

}
