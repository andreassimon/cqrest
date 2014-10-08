package org.cqrest.eventsourcing

import org.apache.commons.logging.Log
import org.apache.commons.logging.LogFactory
import org.springframework.beans.BeansException

import org.springframework.context.ApplicationContext
import org.springframework.context.ApplicationContextAware


class EventProspectDiscoverer implements ApplicationContextAware {
    public static Log log = LogFactory.getLog(this)


    EventSupplier eventSupplier


    EventProspectDiscoverer(EventSupplier eventSupplier) {
        this.eventSupplier = eventSupplier
    }


    @Override
    void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        applicationContext.getBeansWithAnnotation(EventProspect).each {
            if(log.isDebugEnabled()) {
                log.debug "Subscribing `$it.key` to `$eventSupplier`"
            }
            ((EventConsumer)it.value).wasRegisteredAt(eventSupplier)
        }
    }

}
