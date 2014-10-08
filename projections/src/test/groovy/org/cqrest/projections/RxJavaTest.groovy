package org.cqrest.projections

import groovy.transform.Canonical
import org.junit.Test
import rx.Subscription
import rx.observables.GroupedObservable

import static java.util.concurrent.TimeUnit.MILLISECONDS
import static java.util.concurrent.TimeUnit.SECONDS


@SuppressWarnings("GroovyAssignabilityCheck")
class RxJavaTest {

    @Test
    void shouldGroupByAggregateId() {
        final printError = { e -> println("Error encountered $e"); }

        final flattened = rx.Observable.merge(
            customObservableNonBlocking()
            .groupBy({ it.aggregateId })
            .map({
                it
                 .scan(new Printmedium(), { p, event -> p.invokeMethod(event.eventType, event.aggregateId); })
                 .filter({ it.isRenderable() })
                 .buffer(100, MILLISECONDS)
                 .map({
                    it.last()
                 })
            })
//            .map {
//                it.map {
//                    println it; it
//                }
//            }
        )

        flattened
            .subscribe({ println it })

//        flattened
//        .subscribe({ printmedium ->
//                printmedium
//                    .subscribe(
//                    { println(it) },
//                    printError
//                )
//            },
//            printError,
//            { println("Sequence complete"); }
//        )
//
//        sleep 30000
    }

    def customObservableNonBlocking() {
        return rx.Observable.create({ rx.Observer<Map> observer ->
            // For simplicity this example uses a Thread instead of an ExecutorService/ThreadPool
            final t = Thread.start {
                [
                    [aggregateId: UUID.fromString('16f48d37-9b9d-4430-9da6-0969f14534bb'), eventType: 'Printmedium wurde angelegt'],
                    [aggregateId: UUID.fromString('f28729e1-ab42-4104-8d38-430c0e58f9f3'), eventType: 'Printmedium wurde angelegt'],
                    [aggregateId: UUID.fromString('16f48d37-9b9d-4430-9da6-0969f14534bb'), eventType: 'Printmedium wurde geaendert'],
                    [aggregateId: UUID.fromString('728c65f9-eaf4-4768-82c0-81931cbb8112'), eventType: 'Printmedium wurde angelegt'],
                ].each {
                    observer.onNext(it)
                    sleep 1000
                }
                // after sending all values we complete the sequence
                observer.onCompleted();
            }

            return new Subscription() {
                public void unsubscribe() {
                    // Ask the thread to stop doing work.
                    // For this simple example it just interrupts.
                    t.interrupt();
                }
            };
        })
    }
}

@Canonical
class Printmedium {

    UUID id

    def "Printmedium wurde angelegt"(UUID id) {
        this.id = id
        return this
    }

    def "Printmedium wurde geaendert"(UUID id) {
        return this
    }

    boolean isRenderable() {
        return id != null
    }
}
