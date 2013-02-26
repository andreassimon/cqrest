package de.oneos.eventstore


// TODO Write unit tests
class SequenceNumberGenerator<K> {

    Map<K, Integer> sequences = [:]

    int getAt(K key) {
        if(!sequences.containsKey(key)) {
            sequences[key] = 0
        }
        return sequences[key]++
    }

    void putAt(K key, int value) {
        sequences[key] = value
    }

    static Closure<SequenceNumberGenerator> newInstance = {
        new SequenceNumberGenerator()
    }

}
