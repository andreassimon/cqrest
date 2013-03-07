package de.oneos.eventsourcing

import de.oneos.eventsourcing.Event
import groovy.json.JsonBuilder

class GenericEventSerializer {

    static String toJSON(Event<?> event) {
        new JsonBuilder(event.attributes()).toString()
    }

}
