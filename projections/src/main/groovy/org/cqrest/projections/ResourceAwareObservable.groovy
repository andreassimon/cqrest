package org.cqrest.projections

import rx.lang.groovy.GroovyFunctionWrapper
import rx.observables.GroupedObservable
import rx.util.functions.Func2

import org.cqrest.eventsourcing.EventEnvelope


class ResourceAwareObservable<T> {

    @Delegate
    org.cqrest.reactive.Observable<T> wrappee

    ResourceAwareObservable(org.cqrest.reactive.Observable<T> wrappee) {
        this.wrappee = wrappee
    }

    public <R> ResourceAwareObservable<R> transformBodies(Closure<R> func) {
        new ResourceAwareObservable(
          map({ Resource resource ->
              resource.transform(func)
          })
        )
    }

    public ResourceAwareObservable<T> filterBodies(Closure<Boolean> predicate) {
        new ResourceAwareObservable<T>(filter { Resource r -> predicate.call(r.body) })
    }

    public <A> ResourceAwareObservable<Resource<A>> foldAggregateResource(Class<A> aggregateModel) {
        assert aggregateModel

        // The inferred type warning could be fixed by writing `Resource<A>`, but that makes the code crash when run.
        foldAggregateResource(aggregateModel) { Resource resource, EventEnvelope event ->
            resource.apply(event)
        }
    }

    public <B> ResourceAwareObservable<Resource<B>> foldAggregateResource(Class<B> resourceBody, Closure<Resource<B>> foldFunc) {
        assert resourceBody
        assert foldFunc

        new ResourceAwareObservable(groupBy({ it.aggregateId }))
          .foldResource(resourceBody, foldFunc)
    }

    public <B> ResourceAwareObservable<Resource<B>> foldResource(Class<B> resourceBody, Closure<Resource<B>> foldFunc) {
        assert resourceBody
        assert foldFunc

        new ResourceAwareObservable<Resource<B>>(flatMap({ GroupedObservable<UUID, EventEnvelope> group ->
            group.scan(new Resource<B>(
              aggregateId: group.key,
              body: resourceBody.newInstance()
            ), new GroovyFunctionWrapper(foldFunc) as Func2<Resource<B>, EventEnvelope, Resource<B>>)
        }) as org.cqrest.reactive.Observable<Resource<B>>)
    }

}
