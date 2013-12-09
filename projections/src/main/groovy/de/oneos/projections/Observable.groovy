package de.oneos.projections

import de.oneos.eventsourcing.*

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

    public <R> Observable<Resource<R>> transformBodies(Closure<R> func) {
        map({ Resource resource ->
            resource.transform(func)
        })
    }

    Observable<T> filter(Closure predicate) {
        return new Observable<T>(wrappee.filter(new GroovyFunctionWrapper<>(predicate)))
    }

    Observable<T> filterBodies(Closure<Boolean> predicate) {
        filter { Resource it -> predicate.call(it.body) }
    }

    public <R> Observable<R> flatMap(Closure<R> func) {
        return new Observable<R>(wrappee.flatMap(new GroovyFunctionWrapper(func)))
    }

    def subscribe(Closure onNext, Action1 onError, Action0 onComplete) {
        return wrappee.subscribe(new GroovyActionWrapper<>(onNext), onError, onComplete)
    }

    def deposit(Depository<T> depository) {
        return subscribe(depository.&put, Rx.logReactiveError(depository.log), Rx.logSequenceFinished(depository.log))
    }

    def deposit(ResourceDepository<T> depository) {
        return subscribe(depository.&put, Rx.logReactiveError(depository.log), Rx.logSequenceFinished(depository.log))
    }

    public <A> Observable<Resource<A>> foldAggregateResource(Class<A> aggregateModel) {
        assert aggregateModel
        return groupBy({ it.aggregateId })
              .flatMap({ GroupedObservable<UUID, EventEnvelope> aggregateStream ->
                  aggregateStream
                      .scan(new Resource<>(aggregateId: aggregateStream.key, body: aggregateModel.newInstance()), new GroovyFunctionWrapper<>({ Resource resource, EventEnvelope event ->
                          resource.transform { body ->
                              body.invokeMethod(event.eventName, event.eventAttributes)
                              return body
                          }.updateCorrelationId(event.correlationId).
                            updateLastModified(event.timestamp).
                            updateVersion(event.sequenceNumber)
                      }))
              })
    }

    public Observable<Resource> foldResource(Class resourceBody, Closure func) {
        flatMap({ GroupedObservable<UUID, Map> group ->
            group.scan(new Resource<Collection<Map>>(
                aggregateId: group.key,
                body: resourceBody.newInstance()
            ), new GroovyFunctionWrapper<>(func))
        }) as Observable<Resource>
    }

    ConnectableObservable<T> publish() {
        return new ConnectableObservable<T>(wrappee.publish())
    }

}
