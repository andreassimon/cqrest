package infrastructure.utilities

import domain.events.Event
import groovy.json.JsonBuilder

class GenericEventSerializer {

    static String toJSON(Event<?> event) {
        new JsonBuilder(event.attributes()).toString()
    }

}
