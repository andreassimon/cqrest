package org.cqrest.eventsourcing.test

class Util {

    static String abbreviate(UUID id) {
        id.toString()[0..7] + '...'
    }

}
