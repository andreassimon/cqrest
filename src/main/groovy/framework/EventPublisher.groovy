package framework

import domain.events.Event

public interface EventPublisher {

    void publish(Event event)

}
