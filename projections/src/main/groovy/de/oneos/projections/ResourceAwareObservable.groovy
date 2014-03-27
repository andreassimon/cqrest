package de.oneos.projections

import rx.lang.groovy.GroovyFunctionWrapper
import rx.observables.GroupedObservable

import de.oneos.eventsourcing.EventEnvelope


class ResourceAwareObservable<T> {

    @Delegate
    de.oneos.projections.Observable<T> wrappee

    ResourceAwareObservable(de.oneos.projections.Observable<T> wrappee) {
        this.wrappee = wrappee
    }

    public <A> Observable<Resource<A>> foldAggregateResource(Class<A> aggregateModel) {
        assert aggregateModel

        // The type error could be fixed by writing `Resource<A>`, but that makes the code crash when run.
        foldAggregateResource(aggregateModel) { Resource resource, EventEnvelope event ->
            resource.apply(event)
        }
    }

    public <B> Observable<Resource<B>> foldAggregateResource(Class<B> resourceBody, Closure<Resource<B>> foldFunc) {
        assert resourceBody
        assert foldFunc

        new ResourceAwareObservable(groupBy({ it.aggregateId }))
          .foldResource(resourceBody, foldFunc)
    }

    public <B> Observable<Resource<B>> foldResource(Class<B> resourceBody, Closure<Resource<B>> foldFunc) {
        assert resourceBody
        assert foldFunc

        flatMap({ GroupedObservable<UUID, Map> group ->
            group.scan(new Resource<B>(
              aggregateId: group.key,
              body: resourceBody.newInstance()
            ), new GroovyFunctionWrapper<>(foldFunc))
        }) as Observable<Resource<B>>
    }

}
