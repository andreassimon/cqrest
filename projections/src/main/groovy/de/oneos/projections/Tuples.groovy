package de.oneos.projections

import groovy.transform.*


class Tuples {

    public static <A, B> Tuple2<A, B> tuple(A a, B b) {
        return new Tuple2(a, b)
    }

    public static <A, B, C> Tuple3<A, B, C> tuple(A a, B b, C c) {
        return new Tuple3(a, b, c)
    }
}

@Canonical
class Tuple2<A, B> {
    A a
    B b
}

@Canonical
class Tuple3<A, B, C> {
    A a
    B b
    C c
}
