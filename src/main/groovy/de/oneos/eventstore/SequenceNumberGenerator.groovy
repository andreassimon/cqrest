package de.oneos.eventstore


class SequenceNumberGenerator<K> {

    Map<K, Integer> sequences = [:]

    int getAt(K key) {
        if(!sequences.containsKey(key)) {
            sequences[key] = 0
        }
        return sequences[key]++
    }

    static Closure<SequenceNumberGenerator> newInstance = {
        new SequenceNumberGenerator()
    }

}
