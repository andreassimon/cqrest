package de.oneos.eventsourcing.test

class Util {

    public static <T> void pairwise(Iterable<T> left, Iterable<T> right, Closure<?> closure) {
        pairwise(left.iterator(), right.iterator(), closure)
    }

    public static <T> void pairwise(Iterator<T> left, Iterator<T> right, Closure<?> closure) {
        while(left.hasNext() && right.hasNext()) {
            closure(left.next(), right.next())
        }
        while(left.hasNext()) {
            closure(left.next(), null)
        }
        while(right.hasNext()) {
            closure(null, right.next())
        }
    }

    static String abbreviate(UUID id) {
        id.toString()[0..7] + '...'
    }

}
