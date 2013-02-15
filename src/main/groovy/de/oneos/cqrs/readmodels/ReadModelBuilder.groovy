package de.oneos.cqrs.readmodels

import static java.lang.String.format
import org.apache.commons.logging.Log
import org.apache.commons.logging.LogFactory

class ReadModelBuilder {
    private static final Log log = LogFactory.getLog(ReadModelBuilder.class);

    public void project(Map<String, String> eventFilter, Closure projectionFunction) {
        log.debug(format("Creating projection for %s > %s ...", getClazz().getSimpleName(), eventFilter.get("eventName")));

        Projection projection = new Projection();
        projection.setEventName(eventFilter.get("eventName"));
        projection.setHandleEvent(projectionFunction);
        projections.add(projection);

        log.debug(format("... added projection %s", projection));
    }

}
