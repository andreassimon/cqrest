package de.oneos.eventsourcing.test

class Util {

    static String abbreviate(UUID id) {
        id.toString()[0..7] + '...'
    }

}
